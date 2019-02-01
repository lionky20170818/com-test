package com.ligl.common.aspect;

import com.baidu.unbiz.fluentvalidator.FluentValidator;
import com.baidu.unbiz.fluentvalidator.Result;
import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;
import com.xforceplus.athena.activemq.Queues;
import com.xforceplus.athena.activemq.salesbill.localcache.LocalCacheManager;
import com.xforceplus.athena.api.spec.common.model.*;
import com.xforceplus.athena.common.FileDownloadController;
import com.xforceplus.athena.common.LoggedMessageService;
import com.xforceplus.athena.common.RedisLock;
import com.xforceplus.athena.common.SalesBillPermissionTools;
import com.xforceplus.athena.constant.enums.*;
import com.xforceplus.athena.domain.jooq.Tables;
import com.xforceplus.athena.domain.jooq.tables.pojos.*;
import com.xforceplus.athena.exception.SalesbillHandleException;
import com.xforceplus.athena.exception.SalesbillQueryException;
import com.xforceplus.athena.publisher.EventPublisher;
import com.xforceplus.athena.publisher.event.SalesBillChangeEvent;
import com.xforceplus.athena.salesBill.common.*;
import com.xforceplus.athena.salesBill.config.SalesBillConfigService;
import com.xforceplus.athena.salesBill.domain.GetHeaderInfoReq;
import com.xforceplus.athena.salesBill.domain.GetUserConfigRuleReq;
import com.xforceplus.athena.salesBill.history.SalesBillHisServiceImpl;
import com.xforceplus.athena.salesBill.importfile.SalesBillImportFileHandler;
import com.xforceplus.athena.salesBill.rollback.SalesBillMergeAndSplitRollBackService;
import com.xforceplus.athena.salesBill.split.SalesBillSplitService;
import com.xforceplus.athena.salesBill.validator.SalesBillMergeValidator;
import com.xforceplus.athena.salesBill.void_delete_cooperation.SalesBillVoidAndDeleteCooperation;
import com.xforceplus.athena.utils.*;
import com.xforceplus.xplat.core.api.ContextService;
import com.xforceplus.xplat.core.service.TransactionalServiceBase;
import com.xforceplus.xplat.domain.Response;
import com.xforceplus.zeus.inner.dto.FundsTag;
import com.xforceplus.zeus.inner.dto.TaxCatalogItem;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SelectField;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.baidu.unbiz.fluentvalidator.ResultCollectors.toSimple;
import static com.xforceplus.athena.constant.CoreConstants.LIMIT_COUNT;
import static com.xforceplus.athena.domain.jooq.Tables.*;
import static com.xforceplus.athena.salesBill.common.SalesBillTools.checkUserRole;
import static com.xforceplus.athena.salesBill.common.SalesBillTools.defaultSalesBillHeader;


/**
 * 项目名称: 票易通IMSC
 * 模块名称:
 * 说明:
 * JDK 版本: JDK1.8
 * @author ：tanbenshuai/xiangchangjie
 * 创建日期：2017/2/27
 */
@Service
public class SalesBillService extends TransactionalServiceBase {
    static final Logger logger = LoggerFactory.getLogger(SalesBillService.class);
    @Autowired
    private LocalCacheManager localCacheManager;
    @Autowired
    private SalesBillEsTools salesBillEsTools;
    @Autowired
    private SalesBillImportFileHandler salesBillImportFileHandler;
    @Autowired
    private SalesBillTools salesBillTools = null;
    @Autowired
    private ContextService contextService = null;
    @Autowired
    private SalesBillPoolTools salesBillPollTools = null;
    @Autowired
    private LoggedMessageService loggedMessageService  = null;
    @Autowired
    private EventPublisher publisher = null;
    @Autowired
    SalesBillPoolService salesBillPoolService = null;
    @Autowired
    SalesBillPermissionTools salesBillPermissionTools = null;
    @Autowired
    private SalesBillPoolTools salesBillPoolTools = null;
    @Autowired
    private SalesBillMergeAndSplitRollBackService salesBillMergeAndSplitRollBackService;
    @Autowired
    private SalesBillSplitService salesBillSplitService;
    @Autowired
    private SalesBillConfigTools salesBillConfigTools;
    @Autowired
    private SalesBillConfigService salesBillConfigService;
    @Autowired
    private SalesBillHisServiceImpl salesBillHisService;
    @Autowired
    private SalesBillVoidAndDeleteCooperation salesBillVoidAndDeleteCooperation;
    @Autowired
    private CooperationUtils cooperationUtils;
    @Autowired
    private SalesBillHistoryTools historyTools;
    @Autowired
    private SalesBillWithEsService salesBillWithEsService;

    public List<SalesbillItem> getSalesBillList(List<SalesBillConditionRequest> conditions,String catalog, Integer userRole, Integer page, Integer row) {
        try{
            List<SalesbillItem> salesbillItemList = null;
            checkUserRole(userRole);
            //参数为空时，设置默认值
            if (catalog == null) {
                //            catalog = SalesBillStatus.PENDING_PROCESS.value();
                catalog = SalesBillCatalog.WAITINGFORHANDLE.value();
            }
            if (page == null || row == null){
                page = 0;
                row = 10;
            }
            //控制权限
            Condition condition=salesBillTools.getConditionByConditions(conditions,catalog,userRole);
            condition=condition.and(salesBillPermissionTools.getSalesBillPermissionCondition(userRole));
            int offset = page  * row;
            salesbillItemList=create.select()
                    .from(Tables.T_SALES_BILL)
                    .where(condition)
                    .orderBy(Tables.T_SALES_BILL.CREATE_TIME.desc())
                    .offset(offset)
                    .limit(row)
                    .fetchInto(SalesbillItem.class);
            return salesbillItemList;
        }catch (Exception e){
            logger.error("getSalesBillList error",e);
        }
        return Lists.newArrayList();
    }

    public List<SalesbillItem> getSalesBillHeaderList(QueryConditionRequest conditions,String catalog, Integer userRole, Integer page, Integer row) {
        List<SalesbillItem> salesBillItemList = Lists.newLinkedList();
        checkUserRole(userRole);
        List<String> fields = Lists.newArrayList();
        String tenantCode = contextService.getAttributeByType("TENANT_CODE", String.class);
        if(!CommonTools.isEmpty(tenantCode)){
            GetHeaderInfoReq req = new GetHeaderInfoReq();
            req.setSellerTenantCode(tenantCode);
            fields = salesBillConfigTools.getHeadRule(req);
        }
        Collection<Field> groupField = FieldsTools.getGroupFieldName(fields);
        Collection<SelectField<?>> selectField = FieldsTools.getSelectFieldName(fields);
        try{
            //参数为空时，设置默认值
            if (catalog == null) {
                catalog = SalesBillCatalog.WAITINGFORHANDLE.value();
            }
            if (page == null || row == null){
                page = 0;
                row = 10;
            }
            //控制权限
            Condition condition=salesBillTools.getConditionByConditions(conditions.getQueryConditions(),catalog,userRole);
            condition=condition.and(salesBillPermissionTools.getSalesBillPermissionCondition(userRole));

            int offset = page  * row;
            salesBillItemList = create.select(selectField)
                    .from(Tables.T_SALES_BILL)
                    .where(condition)
                    .groupBy(groupField)
                    .orderBy(Tables.T_SALES_BILL.CREATE_TIME.desc())
                    .offset(offset)
                    .limit(row)
                    .fetchInto(SalesbillItem.class);
        }catch (Exception e){
            logger.error("getSalesBillHeaderList error:{}",e);
        }
        return salesBillItemList;
    }

    public SalesBillAmountStatistics getSalesBillListAmountStatistics(List<SalesBillConditionRequest> conditions, String catalog, Integer userRole) {
        checkUserRole(userRole);
        SalesBillAmountStatistics bean = new SalesBillAmountStatistics();
        SearchResponse searchResponse = salesBillEsTools.getSalesBillListAmountStatistics(conditions, catalog, userRole);
        logger.info(searchResponse.toString());
        bean.setAmountWithoutTax(((InternalSum)searchResponse.getAggregations().get("amountWithoutTax")).getValue()+"");
        bean.setTaxAmount(((InternalSum)searchResponse.getAggregations().get("taxAmount")).getValue()+"");
        bean.setAmountWithTax(((InternalSum)searchResponse.getAggregations().get("amountWithTax")).getValue()+"");
        bean.setDiscountWithoutTax(((InternalSum)searchResponse.getAggregations().get("discountWithoutTax")).getValue()+"");
        bean.setDiscountTax(((InternalSum)searchResponse.getAggregations().get("discountTax")).getValue()+"");
        bean.setDiscountWithTax(((InternalSum)searchResponse.getAggregations().get("discountWithTax")).getValue()+"");
        bean.setDeduction(((InternalSum)searchResponse.getAggregations().get("deduction")).getValue()+"");
        return bean;
    }

    public SalesBillCheckAmountStatistics getSalesBillListCheckAmountStatistics(ConditionReqList reqList, Integer userRole) {
        checkUserRole(userRole);
        SalesBillCheckAmountStatistics bean = new SalesBillCheckAmountStatistics();
        String catalog = reqList.getCatalog();
       // SearchResponse searchResponse = salesBillEsTools.getSalesBillListCheckAmountStatistics(conditions, catalog, userRole);
       // SearchResponse searchResponse = salesBillWithEsService.getSalesBillIds(reqList, catalog, userRole, LIMIT_COUNT);
        //获取salesBill Id
        List<String> data = salesBillWithEsService.getSalesBillIds(reqList, catalog, userRole, LIMIT_COUNT);

        List conditions = Lists.newArrayList();
        conditions.add(Tables.T_SALES_BILL.SALES_BILL_ID.in(data));
        List<TSalesBillObj> salesBillObjList = dao.queryObj(Tables.T_SALES_BILL,conditions);

        BigDecimal amountWithoutTax = BigDecimal.ZERO;
        BigDecimal amountWithTax = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountWithoutTax = BigDecimal.ZERO;
        BigDecimal discountTax = BigDecimal.ZERO;
        BigDecimal discountWithTax = BigDecimal.ZERO;
        BigDecimal deduction = BigDecimal.ZERO;

        for(TSalesBillObj tSalesBillObj : salesBillObjList)
        {
            amountWithoutTax = amountWithoutTax.add(tSalesBillObj.getAmountWithoutTax());
            taxAmount = taxAmount.add(tSalesBillObj.getTaxAmount());
            amountWithTax = amountWithTax.add(tSalesBillObj.getAmountWithTax());
            discountWithoutTax = discountWithoutTax.add(tSalesBillObj.getDiscountWithoutTax());
            discountTax = discountTax.add(tSalesBillObj.getDiscountTax());
            discountWithTax = discountWithTax.add(tSalesBillObj.getDiscountWithTax());
            deduction = deduction.add(tSalesBillObj.getDeduction());
        }

        logger.info(salesBillObjList.toString());
        bean.setAmountWithoutTax(amountWithoutTax + "");
        bean.setTaxAmount(taxAmount + "");
        bean.setAmountWithTax(amountWithTax + "");
        bean.setDiscountWithoutTax(discountWithoutTax + "");
        bean.setDiscountTax(discountTax + "");
        bean.setDiscountWithTax(discountWithTax + "");
        bean.setDeduction(deduction + "");

        return bean;
    }

    public Map<String, String> calcPriceQuantity(BigDecimal amountWithTax, BigDecimal amountWithoutTax, BigDecimal unitPrice, BigDecimal unitPriceWithTax, BigDecimal quantity){
        //feature-15405
        Map<String, String> bean = new HashMap<>();
        bean.put("amountWithTax", amountWithTax.toPlainString());
        bean.put("amountWithoutTax", amountWithoutTax.toPlainString());
        if(unitPrice != null) {
            BigDecimal quantity2 = AmountTools.calcQuantity0(amountWithoutTax, unitPrice);
            BigDecimal unitPriceWithTax2 = AmountTools.calcUnitPriceWithTax(amountWithTax, quantity2);
            bean.put("unitPrice", unitPrice.toPlainString());
            bean.put("quantity", quantity2.toPlainString());
            bean.put("unitPriceWithTax", unitPriceWithTax2.toPlainString());
        }else if(unitPriceWithTax != null) {
            BigDecimal quantity2 = AmountTools.calcQuantity1(amountWithTax, unitPriceWithTax);
            BigDecimal unitPrice2 = AmountTools.calcUnitPrice(amountWithoutTax, quantity2);
            bean.put("unitPriceWithTax", unitPriceWithTax.toPlainString());
            bean.put("quantity", quantity2.toPlainString());
            bean.put("unitPrice", unitPrice2.toPlainString());
        }else if(quantity != null) {
            BigDecimal unitPrice2 = AmountTools.calcUnitPrice(amountWithoutTax, quantity);
            BigDecimal unitPriceWithTax2 = AmountTools.calcUnitPriceWithTax(amountWithTax, quantity);
            bean.put("quantity", quantity.toPlainString());
            bean.put("unitPrice", unitPrice2.toPlainString());
            bean.put("unitPriceWithTax", unitPriceWithTax2.toPlainString());
        }
        return bean;
    }

    public List<SalesbillSummaryItem> getSalesbillSummary(List<SalesBillConditionRequest> conditions, Integer userRole) {
        List<SalesbillSummaryItem> summaryItemList = Lists.newArrayList();
        checkUserRole(userRole);
//        String [] catalogs={"4","5","6","0","1"};
        List<String> catalogs=SalesBillCatalog.getAll();//返回{"0","1","2","3","4","5","6","7"}
        Condition userRoleCondition =  salesBillPermissionTools.getSalesBillPermissionCondition(userRole);
        for (int i=0 ; i <catalogs.size() ; i++ ){
            String catalog = catalogs.get(i);
            SalesbillSummaryItem summaryItem=new SalesbillSummaryItem();
            summaryItem.setCatalog(catalog);
            Condition condition =  salesBillTools.getConditionByConditions(conditions,catalog,userRole);
            if(condition != null) {
                condition = condition.and(userRoleCondition);
            }
            summaryItem.setCount(create.fetchCount(Tables.T_SALES_BILL,condition));
            summaryItemList.add(summaryItem);
        }
        return summaryItemList;
    }

    public SalesbillDetail getSalesbillDetailById(String id, List<String> nodeNames, Integer userRole, Integer page, Integer row) {
        SalesbillDetail salesbillDetail = new SalesbillDetail();
        checkUserRole(userRole);
        if (CommonTools.isEmpty(nodeNames) || StringUtils.isEmpty(id) || page == null || row == null){
            throw new SalesbillQueryException("资源码、结算单ID 或分页参数信息不能为空。");
        }
        int offset = page  * row;
        Condition condition=Tables.T_SALES_BILL.SALES_BILL_ID.eq(id);
        condition = condition.and(salesBillPermissionTools.getSalesBillPermissionCondition(userRole));
        SalesbillItem salesbillItem=create.select()
                .from(Tables.T_SALES_BILL)
                .where(condition)
                .offset(offset)
                .limit(row)
                .fetchOneInto(SalesbillItem.class);
        salesbillItem.setQuantity(new BigDecimal(salesbillItem.getQuantity()).stripTrailingZeros().toPlainString());
        salesbillDetail.setSalesbillItem(salesbillItem);
        List<OpreatePermission> permission = getOperationPermission(id, userRole, nodeNames);
        salesbillDetail.setPermission(permission);
        return salesbillDetail;
    }

    public List<OpreatePermission> getOperationPermission(String id, Integer userRole, List<String> nodeNames) {
        List<FundsTag> permissionToolsCheckOpsList =  salesBillTools.getCheckOpsList(id,nodeNames,userRole);
        List<OpreatePermission> resultPermission = permissionToolsCheckOpsList.stream().map(item -> {
            OpreatePermission permission = new OpreatePermission();
            permission.setHasPermission(item.isFlag());
            permission.setNodeEname(item.getNodeEName());
            return permission;
        }).collect(Collectors.toList());
        logger.info("业务单{}权限信息为：{}",id,resultPermission);
        return resultPermission;
    }

    public List<PoolReview> getPoolReview(String poolId, ConditionPoolRequest data, Integer page, Integer row) {
        //feature-1540X
        if (StringUtils.isEmpty(poolId)) {
            return Collections.emptyList();
        }
        Condition conditions = T_SALES_BILL_POOL_PREVIEW.POOL_ID.eq(poolId).and(T_SALES_BILL_POOL_PREVIEW.STATUS.eq(SettlementStatus.NORMAL.value()));
        if(data.getSellerCompanyInfos() != null && !data.getSellerCompanyInfos().isEmpty()) {
            List<CompanyInfo> companies = data.getSellerCompanyInfos();
            Set<String> codes = companies.stream().filter(t -> !StringUtils.isEmpty(t.getCode())).map(t -> t.getCode()).collect(Collectors.toSet());
            Set<String> names = companies.stream().filter(t -> !StringUtils.isEmpty(t.getName())).map(t -> t.getName()).collect(Collectors.toSet());
            Set<String> taxNos = companies.stream().filter(t -> !StringUtils.isEmpty(t.getTaxNo())).map(t -> t.getTaxNo()).collect(Collectors.toSet());
            conditions = conditions.and(T_SALES_BILL_POOL_PREVIEW.SELLER_CODE.in(codes).or(T_SALES_BILL_POOL_PREVIEW.SELLER_NAME.in(names)).or(T_SALES_BILL_POOL_PREVIEW.SELLER_TAX_NO.in(taxNos)));
        }
        if(data.getPurchaserCompanyInfos() != null && !data.getPurchaserCompanyInfos().isEmpty()) {
            List<CompanyInfo> companies = data.getPurchaserCompanyInfos();
            Set<String> codes = companies.stream().filter(t -> !StringUtils.isEmpty(t.getCode())).map(t -> t.getCode()).collect(Collectors.toSet());
            Set<String> names = companies.stream().filter(t -> !StringUtils.isEmpty(t.getName())).map(t -> t.getName()).collect(Collectors.toSet());
            Set<String> taxNos = companies.stream().filter(t -> !StringUtils.isEmpty(t.getTaxNo())).map(t -> t.getTaxNo()).collect(Collectors.toSet());
            conditions = conditions.and(T_SALES_BILL_POOL_PREVIEW.PURCHASER_CODE.in(codes).or(T_SALES_BILL_POOL_PREVIEW.PURCHASER_NAME.in(names)).or(T_SALES_BILL_POOL_PREVIEW.PURCHASER_TAX_NO.in(taxNos)));
        }
        StringBuilder having = new StringBuilder();
        having.append("true");
        if(data.getMinTaxAmount() != null){
            having.append(" and taxAmount - discountTaxAmount >= "+data.getMinTaxAmount());
        }
        if(data.getMaxTaxAmount() != null){
            having.append(" and taxAmount - discountTaxAmount <= "+data.getMaxTaxAmount());
        }
        if(data.getMinAmountWithoutTax() != null){
            having.append(" and amountWithoutTax - discountAmountWithoutTax >= "+data.getMinAmountWithoutTax());
        }
        if(data.getMaxAmountWithoutTax() != null){
            having.append(" and amountWithoutTax - discountAmountWithoutTax <= "+data.getMaxAmountWithoutTax());
        }
        if(data.getMinAmountWithTax() != null){
            having.append(" and amountWithTax - discountAmountWithTax >= "+data.getMinAmountWithTax());
        }
        if(data.getMaxAmountWithTax() != null){
            having.append(" and amountWithTax - discountAmountWithTax <= "+data.getMaxAmountWithTax());
        }
        List<PoolReview> poolReviews = create.select(T_SALES_BILL_POOL_PREVIEW.SETTLEMENT_ID,
                T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY,
                T_SALES_BILL_POOL_PREVIEW.PURCHASER_NAME,
                T_SALES_BILL_POOL_PREVIEW.PURCHASER_TAX_NO,
                T_SALES_BILL_POOL_PREVIEW.PURCHASER_NO,
                T_SALES_BILL_POOL_PREVIEW.SELLER_NAME,
                T_SALES_BILL_POOL_PREVIEW.SELLER_TAX_NO,
                T_SALES_BILL_POOL_PREVIEW.SELLER_NO,
                T_SALES_BILL_POOL_PREVIEW.BUSINESS_BILL_TYPE,
                T_SALES_BILL_POOL_PREVIEW.EXT1,
                T_SALES_BILL_POOL_PREVIEW.EXT2,
                T_SALES_BILL_POOL_PREVIEW.EXT3,
                DSL.count().as("settlementNumber"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.AMOUNT_WITH_TAX,BigDecimal.ZERO)).as("amountWithTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.AMOUNT_WITHOUT_TAX,BigDecimal.ZERO)).as("amountWithoutTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.TAX_AMOUNT,BigDecimal.ZERO)).as("taxAmount"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_WITHOUT_TAX,BigDecimal.ZERO)).as("discountAmountWithoutTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_TAX,BigDecimal.ZERO)).as("discountTaxAmount"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_WITH_TAX,BigDecimal.ZERO)).as("discountAmountWithTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DEDUCTION,BigDecimal.ZERO)).as("deductions"))
                .from(T_SALES_BILL_POOL_PREVIEW)
                .where(conditions)
                .groupBy(T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY)
                .having(having.toString())
                .orderBy(T_SALES_BILL_POOL_PREVIEW.SELLER_NAME.asc(),
                        T_SALES_BILL_POOL_PREVIEW.PURCHASER_NAME.asc(),
                        T_SALES_BILL_POOL_PREVIEW.EXT1.asc())
                .offset(page * row)
                .limit(row)
                .fetchInto(PoolReview.class);
        for (PoolReview review : poolReviews) {
            //重新计算业务单条数
            int salesBillCount = 0;
            List<String> settlementIds = create.select(Tables.T_SALES_BILL_POOL_PREVIEW.SETTLEMENT_ID)
                    .from(T_SALES_BILL_POOL_PREVIEW)
                    .where(T_SALES_BILL_POOL_PREVIEW.POOL_ID.eq(poolId)
                    .and(T_SALES_BILL_POOL_PREVIEW.STATUS.eq(SettlementStatus.NORMAL.value())))
                    .and(T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY.eq(review.getSellerPurchaserHasyKey()))
                    .fetchInto(String.class);
            if(!CommonTools.isEmpty(settlementIds)){
                String[] settlementId = new String[settlementIds.size()];
                settlementIds.toArray(settlementId);
                salesBillCount = salesBillTools.getSalesBillListCount(SalesBillCatalog.ALREADY_JOIN_POLL.value(),1,settlementId);
            }
            //feature-15324
            review.setSalesBillNumber(salesBillCount);
//        a.汇总【不含税金额】 = Round(不含税金额 - 不含税折扣金额, 2)
//        b.汇总【税额】 = Round(税额 - 折扣税额, 2)
//        c.汇总【含税金额】 = Round(含税金额 - 含税折扣金额, 2)
            BigDecimal amountWithoutTax = new BigDecimal(review.getAmountWithoutTax()).subtract(new BigDecimal(review.getDiscountAmountWithoutTax()));
            review.setAmountWithoutTax(amountWithoutTax.toPlainString());
            BigDecimal taxAmount = new BigDecimal(review.getTaxAmount()).subtract(new BigDecimal(review.getDiscountTaxAmount()));
            review.setTaxAmount(taxAmount.toPlainString());
            BigDecimal amountWithTax = new BigDecimal(review.getAmountWithTax()).subtract(new BigDecimal(review.getDiscountAmountWithTax()));
            review.setAmountWithTax(amountWithTax.toPlainString());
            review.setRuleName(getRuleName(poolId, review.getSellerPurchaserHasyKey()));
        }
        return poolReviews;
    }

    public int getPoolReviewCount(String poolId, List<String> keys) {
        Condition conditions = T_SALES_BILL_POOL_PREVIEW.POOL_ID.eq(poolId)
                .and(T_SALES_BILL_POOL_PREVIEW.STATUS.eq(SettlementStatus.NORMAL.value()))
                .and(T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY.in(keys));
        int poolReviewCount = create.selectCount()
                .from(T_SALES_BILL_POOL_PREVIEW)
                .where(conditions)
                .fetchOne(0, int.class);

        return poolReviewCount;
    }

    public List<PoolReview> getPoolReview2(String poolId, List<String> keys, Integer page, Integer row) {
        Condition conditions = T_SALES_BILL_POOL_PREVIEW.POOL_ID.eq(poolId)
                .and(T_SALES_BILL_POOL_PREVIEW.STATUS.eq(SettlementStatus.NORMAL.value()))
                .and(T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY.in(keys));
        List<PoolReview> poolReviews = create.select(T_SALES_BILL_POOL_PREVIEW.SETTLEMENT_ID,
                T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY,
                T_SALES_BILL_POOL_PREVIEW.PURCHASER_NAME,
                T_SALES_BILL_POOL_PREVIEW.PURCHASER_TAX_NO,
                T_SALES_BILL_POOL_PREVIEW.PURCHASER_NO,
                T_SALES_BILL_POOL_PREVIEW.SELLER_NAME,
                T_SALES_BILL_POOL_PREVIEW.SELLER_TAX_NO,
                T_SALES_BILL_POOL_PREVIEW.SELLER_NO,
                T_SALES_BILL_POOL_PREVIEW.BUSINESS_BILL_TYPE,
                T_SALES_BILL_POOL_PREVIEW.EXT1,
                T_SALES_BILL_POOL_PREVIEW.EXT2,
                T_SALES_BILL_POOL_PREVIEW.EXT3,
                DSL.count().as("settlementNumber"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.AMOUNT_WITH_TAX,BigDecimal.ZERO)).as("amountWithTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.AMOUNT_WITHOUT_TAX,BigDecimal.ZERO)).as("amountWithoutTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.TAX_AMOUNT,BigDecimal.ZERO)).as("taxAmount"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_WITHOUT_TAX,BigDecimal.ZERO)).as("discountAmountWithoutTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_TAX,BigDecimal.ZERO)).as("discountTaxAmount"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_WITH_TAX,BigDecimal.ZERO)).as("discountAmountWithTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DEDUCTION,BigDecimal.ZERO)).as("deductions"))
                .from(T_SALES_BILL_POOL_PREVIEW)
                .where(conditions)
                .groupBy(T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY)
                .orderBy(T_SALES_BILL_POOL_PREVIEW.SELLER_NAME.asc(),
                        T_SALES_BILL_POOL_PREVIEW.PURCHASER_NAME.asc(),
                        T_SALES_BILL_POOL_PREVIEW.EXT1.asc())
                .offset(page * row)
                .limit(row)
                .fetchInto(PoolReview.class);
        List<TSalesBillPoolPreviewObj> previews = create.selectDistinct(Tables.T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY, Tables.T_SALES_BILL_POOL_PREVIEW.SETTLEMENT_ID)
                .from(T_SALES_BILL_POOL_PREVIEW)
                .where(conditions)
                .fetchInto(TSalesBillPoolPreviewObj.class);
        //Map<SELLER_PURCHASER_HASY_KEY, List<SettlementId>>
        Map<String, List<String>> map = new HashMap<>();
        for (TSalesBillPoolPreviewObj preview : previews) {
            String key = preview.getSellerPurchaserHasyKey();
            if(! map.containsKey(key)) {
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(preview.getSettlementId());
        }
        //Map<SELLER_PURCHASER_HASY_KEY, RULE_NAME>
        Map<String, String> ruleNameMap = this.getRuleName(poolId, keys);
        for (PoolReview review : poolReviews) {
            //重新计算业务单条数
            int salesBillCount = 0;
            List<String> settlementIds = map.get(review.getSellerPurchaserHasyKey());
            if(!CommonTools.isEmpty(settlementIds)){
                String[] settlementId = new String[settlementIds.size()];
                settlementIds.toArray(settlementId);
                salesBillCount = salesBillTools.getSalesBillListCount(SalesBillCatalog.ALREADY_JOIN_POLL.value(),1,settlementId);
            }
            review.setSalesBillNumber(salesBillCount);
            BigDecimal amountWithoutTax = new BigDecimal(review.getAmountWithoutTax()).subtract(new BigDecimal(review.getDiscountAmountWithoutTax()));
            review.setAmountWithoutTax(amountWithoutTax.toPlainString());
            BigDecimal taxAmount = new BigDecimal(review.getTaxAmount()).subtract(new BigDecimal(review.getDiscountTaxAmount()));
            review.setTaxAmount(taxAmount.toPlainString());
            BigDecimal amountWithTax = new BigDecimal(review.getAmountWithTax()).subtract(new BigDecimal(review.getDiscountAmountWithTax()));
            review.setAmountWithTax(amountWithTax.toPlainString());
            review.setRuleName(ruleNameMap.get(review.getSellerPurchaserHasyKey()));
        }
        return poolReviews;
    }
    /**
     * @return Map<SELLER_PURCHASER_HASH_KEY, RULE_NAME>
     */
    public Map<String, String> getRuleName(String poolId, List<String> sellerPurchaserHasHKeys) {
        Map<String, String> keyIdMap = new HashMap<>();
        List<TSalesBillMergeSplitRuleObj> rules = create.selectDistinct(T_SALES_BILL_MERGE_SPLIT_RULE.SELLER_PURCHASER_HASH_KEY, T_SALES_BILL_MERGE_SPLIT_RULE.SALES_BILL_CONFIG_ID)
            .from(T_SALES_BILL_MERGE_SPLIT_RULE)
            .where(T_SALES_BILL_MERGE_SPLIT_RULE.POOL_ID.eq(poolId).and(T_SALES_BILL_MERGE_SPLIT_RULE.SELLER_PURCHASER_HASH_KEY.in(sellerPurchaserHasHKeys)))
            .fetchInto(TSalesBillMergeSplitRuleObj.class);
        for(TSalesBillMergeSplitRuleObj rule : rules) {
            keyIdMap.put(rule.getSellerPurchaserHashKey(), rule.getSalesBillConfigId());
        }
        //
        Map<String, String> idRuleMap = new HashMap<>();
        List<TSalesBillConfigObj> configs = create.selectDistinct(T_SALES_BILL_CONFIG.SALES_BILL_CONFIG_ID, T_SALES_BILL_CONFIG.RULE_NAME)
            .from(T_SALES_BILL_CONFIG)
            .where(T_SALES_BILL_CONFIG.SALES_BILL_CONFIG_ID.in(keyIdMap.values()))
            .fetchInto(TSalesBillConfigObj.class);
        for(TSalesBillConfigObj tmp : configs) {
            idRuleMap.put(tmp.getSalesBillConfigId(),tmp.getRuleName());
        }
        //
        Map<String, String> map = new HashMap<>();
        for (String key : sellerPurchaserHasHKeys) {
            String configId = keyIdMap.get(key);
            if (idRuleMap.containsKey(configId)){
                map.put(key, idRuleMap.get(configId));
            }else {
                map.put(key, "默认规则");
            }
        }
        return map;
    }

    public String getRuleName(String poolId, String sellerPurchaserHasHKey) {
        String salesBillConfigId = create.select(T_SALES_BILL_MERGE_SPLIT_RULE.SALES_BILL_CONFIG_ID)
                .from(T_SALES_BILL_MERGE_SPLIT_RULE)
                .where(T_SALES_BILL_MERGE_SPLIT_RULE.POOL_ID.eq(poolId)
                    .and(T_SALES_BILL_MERGE_SPLIT_RULE.SELLER_PURCHASER_HASH_KEY.eq(sellerPurchaserHasHKey)))
                .limit(1)
                .fetchOne(T_SALES_BILL_MERGE_SPLIT_RULE.SALES_BILL_CONFIG_ID);
        if (StringUtils.isEmpty(salesBillConfigId)) {
            return "默认规则";
        }
        String ruleName = create.select(T_SALES_BILL_CONFIG.RULE_NAME)
                .from(T_SALES_BILL_CONFIG)
                .where(T_SALES_BILL_CONFIG.SALES_BILL_CONFIG_ID.eq(salesBillConfigId))
                .limit(1)
                .fetchOne(T_SALES_BILL_CONFIG.RULE_NAME);
        return StringUtils.isEmpty(ruleName) ? "默认规则" : ruleName;
    }

    public SellerPurchaserAndPoolSettlement getPoolSettlement(String poolId, String key, Integer page, Integer row) {
        if(StringUtils.isEmpty(poolId) || StringUtils.isEmpty(key) || page == null || row == null){
            throw new SalesbillQueryException("结算池ID、购销对key 或 分页参数信息不能为空。");
        }
        // feature-15324
        SellerPurchaserAndPoolSettlement returnInfo = new SellerPurchaserAndPoolSettlement();
        SellerPurchaserPair pair = new SellerPurchaserPair();
        List<PoolSettlement> poolSettlementList = Lists.newArrayList();
        int offset = page  * row;
        List<Condition> condition = Lists.newArrayList(
                T_SALES_BILL_POOL_PREVIEW.POOL_ID.eq(poolId),
                T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY.eq(key),
                T_SALES_BILL_POOL_PREVIEW.STATUS.eq(SettlementStatus.NORMAL.value()));
        List<TSalesBillPoolPreviewObj> salesBillPoolPreviewObjs =create.select()
                .from(T_SALES_BILL_POOL_PREVIEW)
                .where(condition)
                .orderBy(T_SALES_BILL_POOL_PREVIEW.SETTLEMENT_NO.desc())
                .offset(offset)
                .limit(row)
                .fetchInto(TSalesBillPoolPreviewObj.class);
        if(salesBillPoolPreviewObjs.size() > 0) {
            TSalesBillPoolPreviewObj tSalesBillPoolPreviewObj = salesBillPoolPreviewObjs.get(0);
            BeanUtils.copy(tSalesBillPoolPreviewObj,pair);
        }
        int salesBillCount = 0;
        for (TSalesBillPoolPreviewObj obj : salesBillPoolPreviewObjs) {
            PoolSettlement settlement = new PoolSettlement();
            BeanUtils.copy(obj,settlement);
            int salesBillNumber = salesBillTools.getSalesBillListCount(SalesBillCatalog.ALREADY_JOIN_POLL.value(),1,obj.getSettlementId());
            settlement.setSalesBillNumber(String.valueOf(salesBillNumber));
            salesBillCount = salesBillCount + salesBillNumber;
            //查询自定义结算单备注信息
            String settlementRemark = salesBillConfigTools.getSettlementRemark(obj);
            settlement.setRemark(settlementRemark);
//            if (!CommonTools.isEmpty(settlementRemark)){
//                settlement.setRemark(settlementRemark);
//            }
//        a.汇总【不含税金额】 = Round(不含税金额 - 不含税折扣金额, 2)
//        b.汇总【税额】 = Round(税额 - 折扣税额, 2)
//        c.汇总【含税金额】 = Round(含税金额 - 含税折扣金额, 2)
            BigDecimal amountWithoutTax = new BigDecimal(settlement.getAmountWithoutTax()).subtract(new BigDecimal(settlement.getDiscountWithoutTax()));
            settlement.setAmountWithoutTax(amountWithoutTax.toPlainString());
            BigDecimal taxAmount = new BigDecimal(settlement.getTaxAmount()).subtract(new BigDecimal(settlement.getDiscountTax()));
            settlement.setTaxAmount(taxAmount.toPlainString());
            BigDecimal amountWithTax = new BigDecimal(settlement.getAmountWithTax()).subtract(new BigDecimal(settlement.getDiscountWithTax()));
            settlement.setAmountWithTax(amountWithTax.toPlainString());
            poolSettlementList.add(settlement);
        }
        returnInfo.setSettlementList(poolSettlementList);
        //
        pair.setSalesBillCount(String.valueOf(salesBillCount));
        int settlementCount = create.selectCount().from(T_SALES_BILL_POOL_PREVIEW)
                .where(T_SALES_BILL_POOL_PREVIEW.POOL_ID.eq(poolId)
                .and(T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY.eq(key))
                .and(T_SALES_BILL_POOL_PREVIEW.STATUS.eq(SettlementStatus.NORMAL.value())))
                .fetchOne().value1();
        logger.info("settlementCount = 数目：{}", settlementCount);
        pair.setSettlementCount(String.valueOf(settlementCount));
        // amount+discount+deduction
        TSalesBillPoolPreviewObj salesResult = create.select(
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.AMOUNT_WITH_TAX,BigDecimal.ZERO)).as("amountWithTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.AMOUNT_WITHOUT_TAX,BigDecimal.ZERO)).as("amountWithoutTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.TAX_AMOUNT,BigDecimal.ZERO)).as("taxAmount"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_WITHOUT_TAX,BigDecimal.ZERO)).as("discountWithoutTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_TAX,BigDecimal.ZERO)).as("discountTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_WITH_TAX,BigDecimal.ZERO)).as("discountWithTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DEDUCTION,BigDecimal.ZERO)).as("deduction"))
                .from(T_SALES_BILL_POOL_PREVIEW)
                .where(T_SALES_BILL_POOL_PREVIEW.POOL_ID.eq(poolId)
                .and(T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY.eq(key))
                .and(T_SALES_BILL_POOL_PREVIEW.STATUS.eq(SettlementStatus.NORMAL.value()))
                ).fetchOneInto(TSalesBillPoolPreviewObj.class);
//        a.汇总【不含税金额】 = Round(不含税金额 - 不含税折扣金额, 2)
//        b.汇总【税额】 = Round(税额 - 折扣税额, 2)
//        c.汇总【含税金额】 = Round(含税金额 - 含税折扣金额, 2)
        try {
            CommonTools.zeroBigDecimalFields(salesResult, TSalesBillPoolPreviewObj.class);
        }catch (Exception e) {
            logger.error("TSalesBillPoolPreviewItemObj中所有BigDecimal字段为null的时候，设置为BigDecimal.ZERO 发生异常", e);
        }
        BigDecimal amountWithoutTax = salesResult.getAmountWithoutTax().subtract(salesResult.getDiscountWithoutTax());
        pair.setAmountWithoutTax(amountWithoutTax.toPlainString());
        BigDecimal taxAmount = salesResult.getTaxAmount().subtract(salesResult.getDiscountTax());
        pair.setTaxAmount(taxAmount.toPlainString());
        BigDecimal amountWithTax = salesResult.getAmountWithTax().subtract(salesResult.getDiscountWithTax());
        pair.setAmountWithTax(amountWithTax.toPlainString());
        pair.setDiscountWithoutTax(salesResult.getDiscountWithoutTax().toPlainString());
        pair.setDiscountTax(salesResult.getDiscountTax().toPlainString());
        pair.setDiscountWithTax(salesResult.getDiscountWithTax().toPlainString());
        pair.setDeduction(salesResult.getDeduction().toPlainString());
        //5876:结算单号单独为普洛斯做配置不修改该信息
        String tenantCode = contextService.getAttributeByType("TENANT_CODE", String.class);
        String modifyNOFlag = "1";//结算单号是否可修改(0-不可修改;1-可修改)
        if (TenantCodeEnum.PLS.getCode().equals(tenantCode)){
            modifyNOFlag = "0";
        }
        pair.setModifyNOFlag(modifyNOFlag);
        returnInfo.setSellerPurchaserInfo(pair);
        return returnInfo;
    }

    public List<PoolSettlementDetail> getPoolSettlementItems(String settlementId, Integer page, Integer row) {
        List<PoolSettlementDetail> poolSettlementDetail = Lists.newArrayList();
        if(StringUtils.isEmpty(settlementId)){
            return  poolSettlementDetail;
        }
        if (page == null || row == null){
            page = 0;
            row = 10;
        }
        int offset = page  * row;
        Condition condition=T_SALES_BILL_POOL_PREVIEW_ITEM.SETTLEMENT_ID.eq(settlementId)
                .and(T_SALES_BILL_POOL_PREVIEW_ITEM.STATUS.eq(SettlementStatus.NORMAL.value()));
        poolSettlementDetail=create.select()
                .from(T_SALES_BILL_POOL_PREVIEW_ITEM)
                .where(condition)
                .offset(offset)
                .limit(row)
                .fetchInto(PoolSettlementDetail.class);
        return poolSettlementDetail;
    }

    public Response settlementRevokeToSalesBill(String settlementId, Integer userRole) {
        checkUserRole(userRole);
        if (StringUtils.isEmpty(settlementId)){
            return Response.failed("结算单ID不能为空");
        }
        // 4-待处理 5-已加入结算池 6-已生成结算单 9-删除 0-作废
        List settlementCondition =Lists.newArrayList();
        settlementCondition.add(T_SETTLEMENT.SETTLEMENT_ID.eq(settlementId));
        List<TSettlementObj> settlementList =  dao.queryObj(T_SETTLEMENT,settlementCondition);
        if (!CollectionUtils.isEmpty(settlementList)) {
            TSettlementObj settlement = settlementList.get(0);
            BigDecimal  alreadyInvoiceAmountWithoutTax = settlement.getAlreadyInvoiceAmountWithoutTax();
            if (alreadyInvoiceAmountWithoutTax.compareTo(BigDecimal.ZERO) !=0){
                return Response.failed("结算单已生成正式发票，撤回失败");
            }
        }

        List conditionLst=Lists.newArrayList();
        conditionLst.add(Tables.T_SALES_BILL.SETTLEMENT_ID.eq(settlementId));
        List<TSalesBillObj> salesBillObjList = dao.queryObj(Tables.T_SALES_BILL,conditionLst);
        if (CollectionUtils.isEmpty(salesBillObjList)){
            return Response.failed("撤回失败，根据结算单号未查询到对应的业务单记录");
        }

        conditionLst.clear();
        conditionLst.add(Tables.T_SALES_BILL.SETTLEMENT_ID.eq(settlementId));
        conditionLst.add(Tables.T_SALES_BILL.UPLOAD_CONFIRM_FLAG.eq(SalesBillCatalog.ALREADY_MAKE.value()));
        conditionLst.add(Tables.T_SALES_BILL.RECEIVE_CONFIRM_FLAG.eq(SalesBillCatalog.ALREADY_MAKE.value()));
        int num = create.update(Tables.T_SALES_BILL)
                .set(Tables.T_SALES_BILL.STATUS,SalesBillStatus.NORMAL.value())
                .set(Tables.T_SALES_BILL.UPLOAD_CONFIRM_FLAG,SalesBillCatalog.WAITINGFORHANDLE.value())
                .set(Tables.T_SALES_BILL.RECEIVE_CONFIRM_FLAG,SalesBillCatalog.WAITINGFORHANDLE.value())
                .set(Tables.T_SALES_BILL.SETTLEMENT_ID,"")
                .set(Tables.T_SALES_BILL.SETTLEMENT_NO,"")
                .set(Tables.T_SALES_BILL.SETTLEMENT_ITEM_ID,"")
                .set(Tables.T_SALES_BILL.UPDATE_TIME,DateTools.getTime17())
                .set(Tables.T_SALES_BILL.USER_ID,"")
                .set(Tables.T_SALES_BILL.USER_NAME,"")
                .set(Tables.T_SALES_BILL.POOL_ID,"")
                .set(Tables.T_SALES_BILL.ADD_POOL_TYPE,"")
                .set(Tables.T_SALES_BILL.COMPUTE_STATUS,"")
                .where(conditionLst).execute();
        if (num != salesBillObjList.size()){
            return Response.failed("撤回失败，业务单状态更新失败,撤回的业务单号："+settlementId+"，业务单数量：" + salesBillObjList.size());
        }

        //记录业务单履历
        for (int i = 0; i < salesBillObjList.size(); i++) {
            TSalesBillObj older = salesBillObjList.get(i);
            TSalesBillObj newer  = new TSalesBillObj();
            BeanUtils.copy(older,newer);
//            newer.setStatus(SalesBillStatus.PENDING_PROCESS.value());
            newer.setUploadConfirmFlag(SalesBillCatalog.WAITINGFORHANDLE.value());
            newer.setReceiveConfirmFlag(SalesBillCatalog.WAITINGFORHANDLE.value());
            newer.setSettlementItemId("");
            newer.setSettlementNo("");
            newer.setSettlementId("");
            SalesBillChangeEvent event = new SalesBillChangeEvent(this, SalesBillAction.REVOKE_FROM_SETTLEMENT, older, newer,"");
            publisher.pubishEvent(event);
        }

        //解除结算单和业务单关联关系
        revokeSalesBillWithSettlement(settlementId);

        return Response.ok("结算单撤回到业务单成功");
    }

    /**
     * 撤销 原始业务单 和 结算单 关联关系
     * @param settlementId 结算单模块撤销作废结算单的 结算单Id
     */
    private void revokeSalesBillWithSettlement(String settlementId){
        logger.info("revokeSalesBillWithSettlement settlementId:{}",settlementId);
        Condition condition = Tables.T_SALES_BILL_SETTLEMENT_REL.SETTLEMENT_ID.eq(settlementId);
        condition = condition.and(Tables.T_SALES_BILL_SETTLEMENT_REL.STATUS.eq(SalesBillStatus.NORMAL.value()));
        int affected = create.update(Tables.T_SALES_BILL_SETTLEMENT_REL)
                .set(Tables.T_SALES_BILL_SETTLEMENT_REL.STATUS,SalesBillStatus.DELETED.value())
                .set(Tables.T_SALES_BILL_SETTLEMENT_REL.UPDATE_TIME,DateTools.getTime17())
                .where(condition).execute();
        if(affected < 1) {
            logger.warn("原始业务单 和 结算单 关联关系 失败!");
        }
        logger.info("revokeSalesBillWithSettlement affected:{}",affected);
    }

    /**
     * 根据ID作废业务单
     * @param reqList
     * @param userRole
     * @param desc
     * @return
     */
    public Response abandonSalesBillByIds(ConditionReqList reqList, Integer userRole, String desc) {
        CommonTools.assertNotNull(reqList);
        checkUserRole(userRole);

        //获取catalog状态栏标记
        String catalog = reqList.getCatalog();
        //获取 salesBillId
        List<String> data = salesBillWithEsService.getSalesBillIds(reqList,catalog,userRole, LIMIT_COUNT);

//        logger.debug("作废业务单[{}],描述[{}]",data,desc);

        if (CommonTools.isEmpty(data)) {
            return Response.failed("要作废的业务单Id集合不能为空!");
        }
        Response response = preAbandonSalesBillByIds(reqList, userRole);
        if (Response.Fail.equals(response.getCode())){
            return response;
        }
        Map result = (Map) response.getResult();
        List<TSalesBillObj> salesBillObjList = (List<TSalesBillObj>)result.get("successSalesBillObj");
        List<TSalesBillObj> newSalesBillList = Lists.newArrayList();
        if (CommonTools.isEmpty(salesBillObjList)){
            return Response.from(Response.Fail,"未查到可作废的业务单");
        }
        for (int i = 0; i < salesBillObjList.size() ; i++) {
            TSalesBillObj older = salesBillObjList.get(i);

            TSalesBillObj newer  = new TSalesBillObj();
            BeanUtils.copy(older,newer);
            newer.setStatus(SalesBillStatus.INVALID.value());
            if (!CommonTools.isEmpty(older.getUploadConfirmFlag())){
                newer.setUploadConfirmFlag(SalesBillCatalog.INVALID.value());
            }
            if (!CommonTools.isEmpty(older.getReceiveConfirmFlag())){
                newer.setReceiveConfirmFlag(SalesBillCatalog.INVALID.value());
            }
            //该方法事务正确提交在记录日志
            historyTools.recordHistoryAfterCommit(SalesBillAction.ABANDON_SALES_BILL,older,newer,desc);
            newSalesBillList.add(newer);
        }
        int[] count = updateBatch(Tables.T_SALES_BILL, newSalesBillList);
        logger.info("转入的业务单数量：{}，作废业务单数量{}", data.size(), count);
        salesBillObjList.stream().forEach(t ->
            salesBillVoidAndDeleteCooperation.sendVoidAndDeleteMessageToMQ(t, SalesBillStatus.INVALID, contextService.currentUser())
        );
        return Response.ok("业务单作废成功");
    }

    /**
     * 根据业务单id 批量对已作废的业务单还原（取消作废）
     * @param reqList
     * @param userRole
     * @param desc
     * @return
     */
    public Response cancelAbandonSalesBillByIds(ConditionReqList reqList, Integer userRole, String desc) {
        checkUserRole(userRole);

        //获取catalog状态栏标记
        String catalog = reqList.getCatalog();

        List<String> data = salesBillWithEsService.getSalesBillIds(reqList,catalog,userRole, LIMIT_COUNT);

//        logger.debug("取消作废业务单[{}],描述[{}]",data,desc);

        Response response = preCancelAbandonSalesBillByIds(reqList, userRole);
        if (Response.Fail.equals(response.getCode())){
            return response;
        }
        Map result = (Map) response.getResult();
        List<TSalesBillObj> salesBillObjList = (List<TSalesBillObj>)result.get("successSalesBillObj");
        if (CommonTools.isEmpty(salesBillObjList)){
            return Response.from(Response.Fail,"未查到可取消作废的业务单");
        }
        List<TSalesBillObj> newSalesBillObj = Lists.newArrayList();
        for (int i = 0; i < salesBillObjList.size() ; i++) {
            TSalesBillObj older = salesBillObjList.get(i);
            TSalesBillObj newer  = new TSalesBillObj();
            BeanUtils.copy(older,newer);
//            newer.setStatus(SalesBillStatus.PENDING_PROCESS.value());
            newer.setStatus(SalesBillStatus.NORMAL.value());
            if(!CommonTools.isEmpty(newer.getUploadConfirmFlag())){
                newer.setUploadConfirmFlag(SalesBillCatalog.CONFIRMING.value());
            }
            if (!CommonTools.isEmpty(newer.getReceiveConfirmFlag())){
                newer.setReceiveConfirmFlag(SalesBillCatalog.OPPOSITECONFIRMING.value());
            }
            historyTools.recordHistoryAfterCommit(SalesBillAction.CANCEL_ABANDON_SALES_BILL, older, newer,desc);
            newSalesBillObj.add(newer);
        }
        int[] count = updateBatch(Tables.T_SALES_BILL, newSalesBillObj);
        if(count.length == salesBillObjList.size() ) {
            return Response.ok("取消作废业务单成功!");
        } else {
            return Response.ok("取消作废业务单成功" + count + "条,失败" + (salesBillObjList.size() - count.length) + "条!");
        }
    }

    @SingleDistributedLock
    public boolean dropSalesBillById(String salesBillId, String desc){
        return this.dropSalesBillByIds(Lists.newArrayList(salesBillId), desc);
    }

    public List<String> getCanDropSalesBillIds(List<String> salesBillIds, Integer userRole) {
        String role = salesBillTools.convertRole(userRole);
        return create.select(T_SALES_BILL.SALES_BILL_ID)
                .from(Tables.T_SALES_BILL)
                .where(Tables.T_SALES_BILL.SALES_BILL_ID.in(salesBillIds))
                .and(Tables.T_SALES_BILL.BUSINESS_BILL_TYPE.eq(role))
                .and(Tables.T_SALES_BILL.STATUS.in(SalesBillStatus.INVALID.value()).or(Tables.T_SALES_BILL.STATUS.in(SalesBillStatus.DELETED.value()))) //INVALID: 作废后删除, DELETED: 客户删除处理失败 需要 再次处理
                .and(salesBillPermissionTools.getSalesBillPermissionCondition(userRole))
                .limit(5000) //最多5000条
                .fetchInto(String.class);
    }

    @MultipartDistributedLock
    public boolean dropSalesBillByIds(List<String> salesBillIds, String desc){
        String operateUserId = contextService.currentUser();
        String operateUserName = contextService.currentOperationName();
        historyTools.batchDeleteSalesBill(salesBillIds, operateUserId, operateUserName, desc);
        salesBillVoidAndDeleteCooperation.sendVoidAndDeleteMessageToMQ(salesBillIds, SalesBillStatus.DELETED, contextService.currentUser());
        return true;
    }

    /**
     * @param id
     * @param row
     * @param page
     * @return
     */
    public List<SalesBillHistory> getSalesBillHistory(String id, Integer page, Integer row) {
        if(StringUtils.isEmpty(id) || page == null || row == null){
            throw new SalesbillQueryException("业务单号 或分页参数信息不能为空！");
        }
        int offset = page  * row;
        List<Condition> conditions = Lists.newArrayList(T_SALES_BILL_HISTORY.SALES_BILL_ID.eq(id));
        List<TSalesBillHistoryObj> historyObjs =  queryObj(T_SALES_BILL_HISTORY,conditions, Lists.newArrayList(T_SALES_BILL_HISTORY.OPERATE_TIME.sort(SortOrder.DESC)),offset,row);
        return historyObjs.stream().map(item -> {
            SalesBillHistory history = new SalesBillHistory();
            BeanUtils.copy(item,history);
            return history;
        }).collect(Collectors.toList());
    }

    public Response settlementRevokeToPool(String settlementId, Integer userRole) {
        checkUserRole(userRole);
        if (StringUtils.isEmpty(settlementId)){
            return Response.failed("结算单ID不能为空");
        }
        List conditions= com.google.common.collect.Lists.newArrayList();
        conditions.add(T_SALES_BILL_POOL_PREVIEW.SETTLEMENT_ID.eq(settlementId));
        conditions.add(salesBillPermissionTools.getSalesBillPoolPreviewPermissionCondition(userRole));
        List<TSalesBillPoolPreviewObj> previewObjList = dao.queryObj(T_SALES_BILL_POOL_PREVIEW,conditions);
        if (previewObjList.size()==0){
            return Response.failed("根据结算单ID未查询到记录");
        }
        TSalesBillPoolPreviewObj previewObj =previewObjList.get(0);
        //校验能不能从结算单撤回到结算池

        // 结算单 5-已完成 4-
        List settlementCondition = com.google.common.collect.Lists.newArrayList();
        settlementCondition.add(T_SETTLEMENT.SETTLEMENT_ID.eq(settlementId));
        List<TSettlementObj> settlementList =  dao.queryObj(T_SETTLEMENT,settlementCondition);
        if (settlementList.size()>0){
            TSettlementObj settlement = settlementList.get(0);
            BigDecimal  alreadyInvoiceAmountWithoutTax=  settlement.getAlreadyInvoiceAmountWithoutTax();
            if (!(alreadyInvoiceAmountWithoutTax.compareTo(BigDecimal.ZERO)==0)){
                return Response.failed("结算单已生成正式发票，撤回失败");
            }
        }
        previewObj.setStatus("1");
        dao.update(T_SALES_BILL_POOL_PREVIEW,previewObj);
        return Response.ok("结算单撤回到结算池成功");
    }

    /**
     * 业务单合并前的校验
     * @param merge 待校验业务单集合对象
     * @param userRole 角色
     * @return 校验结果
     */
    
    public Response salesBillMergeValidate(SalesBillMerge merge, Integer userRole) {
        checkUserRole(userRole);
        if (merge == null) {
            return  Response.failed("业务单合并失败，合并数据不能为空参数!");
        }
        List<TSalesBillObj> salesBillObjLst = getMergeSalesBillObjLst(merge.getData(),userRole);
        return salesBillMergeValidate(merge,userRole,salesBillObjLst);
    }

    private Response salesBillMergeValidate(SalesBillMerge merge, Integer userRole, List<TSalesBillObj> salesBillObjLst) {
        try {
            logger.info("进入业务单合并校验方法块：{}，当前时间：{}", new Object[]{"salesBillMergeValidate", System.currentTimeMillis()});
            if(merge == null) {
                return Response.failed("业务单合并失败，参数非法");
            }
            List<String> data = merge.getData();
            if (CollectionUtils.isEmpty(data)) {
                return Response.failed("业务单合并失败，参数非法");
            }

            logger.info("合并的id个数:" + data.size());
            if (data.size() < 2) {
                return Response.failed("业务单合并失败，单条业务单无需合并");
            }

            if (CollectionUtils.isEmpty(salesBillObjLst) || data.size() != salesBillObjLst.size()) {
                return Response.failed("业务单合并失败，选中业务单状态已变更，请刷新页面重新操作");
            }

/*            if(salesBillObjLst.stream().anyMatch(salesBillObj->!SalesBillOrigin.ORIGINAL.value().equals(salesBillObj.getOrigin())) ) {
                return Response.failed("业务单合并失败,待合并的业务单并非都是原始业务单");
            }*/

            Result checkResult = FluentValidator.checkAll().on(salesBillObjLst, new SalesBillMergeValidator()).doValidate().result(toSimple());
            if (!checkResult.isSuccess()) {
                logger.info("校验结果失败，失败原因：{}   失败时间:{}",checkResult.getErrors().toString(), System.currentTimeMillis());
                return Response.failed("业务单合并失败：" + checkResult.getErrors().toString());
            } else {
                return Response.ok("业务单合并校验成功");
            }
        }catch (Exception e) {
            logger.error("业务单合并校验异常，异常信息", e);
            return Response.failed("业务单合并 失败：" + e.getMessage());
        }
    }

    /**
     * 查询合并业务单对象信息
     * @param salesBillIdLst 业务单主键集合
     * @param userRole 角色
     * @return 业务单对象的集合
     */
    private  List<TSalesBillObj> getMergeSalesBillObjLst( List<String> salesBillIdLst, Integer userRole ){
        if (CommonTools.isEmpty(salesBillIdLst)) {
            throw new SalesbillHandleException("业务单合并失败，参数非法");
        }
        if(userRole == null){
            throw new SalesbillHandleException("userRole 不能为空");
        }
        List conditions = Lists.newArrayList();
        conditions.add(Tables.T_SALES_BILL.SALES_BILL_ID.in(salesBillIdLst.stream().distinct().collect(Collectors.toList())));
        conditions.add(salesBillPermissionTools.getSalesBillPermissionCondition(userRole));
        List<TSalesBillObj> salesBillObjList = dao.queryObj(Tables.T_SALES_BILL, conditions);
        logger.info("getMergeSalesBillObjLst salesBillIdLst.size():{}, salesBillObjList.size():{}",salesBillIdLst.size(), (CollectionUtils.isEmpty(salesBillObjList)) ? 0 : salesBillObjList.size());
        return  salesBillObjList;
    }

    /**
     * 业务单合并
     * @param merge 待合并对象
     * @param userRole 角色
     * @return 合并结果
     */
    public Response salesBillMerge(SalesBillMerge merge, Integer userRole) {
        try {
            checkUserRole(userRole);
            if (merge == null) {
                return  Response.failed("业务单合并失败，合并数据不能为空参数!");
            }
            List<TSalesBillObj> salesBillObjLst = getMergeSalesBillObjLst(merge.getData(),userRole);

            Response validateResponse = salesBillMergeValidate(merge, userRole, salesBillObjLst);
            if(!Response.OK.equals(validateResponse.getCode())){
                return validateResponse;
            }
            TSalesBillObj newSalesBill = new TSalesBillObj();
            SalesbillItem salesBillItem = merge.getSalesBillItem();
            //校验前端合并后的参数、前端合并后的结果应该和服务器端合并后的结果一致
            //salesBillItem 中的salesBillId主键 是合并前业务单集合中的 某一个，db中如果不存在checkSalesBillModify 则会报错
            StringBuilder checkMsg = this.validateSalesBillModify(salesBillItem,SalesBillAction.MERGE.value());
            if (!CommonTools.isEmpty(checkMsg.toString())) {
                return Response.failed(checkMsg.toString());
            }
            CommonTools.copyProperties(salesBillItem, newSalesBill);
            // 0： 默认合并策略-不合并 1：数量相加 2：数量取1 3：固定单价 4：清空数据单价
            String quantityPriceResult = merge.getQuantityPriceResult();
            salesBillTools.fulfillMergeSalesBillPrice(salesBillObjLst, newSalesBill,newSalesBill.getTaxRate(),quantityPriceResult);
            newSalesBill.setSalesBillId(CommonTools.getUUID());
            newSalesBill.setCreateTime(DateTools.getTime17());
            newSalesBill.setCreateUserId(contextService.currentUser());
            newSalesBill.setVersionNo(0);
            newSalesBill.setSettlementNo(CommonTools.emptyString());
            newSalesBill.setSettlementId(CommonTools.emptyString());
            newSalesBill.setSettlementItemId(CommonTools.emptyString());
            newSalesBill.setAddPoolType(CommonTools.emptyString());
            newSalesBill.setComputeStatus(CommonTools.emptyString());
            newSalesBill.setOrigin(SalesBillOrigin.MERGE.value());
            //feature-15219
            int maxOriginCount = salesBillObjLst.stream().mapToInt((t) -> t.getOriginCount()).max().getAsInt();
            newSalesBill.setOriginCount(maxOriginCount+1);
            //业务单标志删除
            for (int i = 0; i < salesBillObjLst.size(); i++) {
                TSalesBillObj salesBillObj = salesBillObjLst.get(i);
                //salesBillObj.setSalesBillId(salesBillObj.getSalesBillId());
                salesBillObj.setStatus(SalesBillStatus.DELETED.value());
                salesBillObj.setUploadConfirmFlag("");
                salesBillObj.setReceiveConfirmFlag("");
                salesBillObj.setDeleteToken(CommonTools.getUUID());
                salesBillObj.setDeleteTime(DateTools.getTime17());
                salesBillObj.setUpdateTime(DateTools.getTime17());
                salesBillObj.setDeleteUserId(contextService.currentUser());
                //feature-15219
                salesBillObj.setParentSalesBillId(newSalesBill.getSalesBillId());
                if(salesBillObj.getSalesBillNo().equals(newSalesBill.getSalesBillNo())){
                    newSalesBill.setItemShortName(salesBillObj.getItemShortName());
                }
                logger.info("合并业务单[{}]时删除的业务单[{}]，数量:{}，单价:{},税率:{},不含税金额:{},税额:{},含税金额:{}",newSalesBill.getSalesBillId(),salesBillObj.getSalesBillId()
                        ,salesBillObj.getQuantity(),salesBillObj.getUnitPrice(),salesBillObj.getTaxRate(), salesBillObj.getAmountWithoutTax(),salesBillObj.getTaxAmount(),salesBillObj.getAmountWithTax());
            }
            logger.info("合并后的业务单:{}，数量:{}，单价:{},税率:{},不含税金额:{},税额:{},含税金额:{}",newSalesBill.getSalesBillId()
                    ,newSalesBill.getQuantity(),newSalesBill.getUnitPrice(),newSalesBill.getTaxRate(), newSalesBill.getAmountWithoutTax(),newSalesBill.getTaxAmount(),newSalesBill.getAmountWithTax());

            //更新 插入本地库
            updateBatch(Tables.T_SALES_BILL,salesBillObjLst);
            insert(Tables.T_SALES_BILL, newSalesBill);
            historyTools.recordHistoryAfterCommit(SalesBillAction.MERGE, newSalesBill, newSalesBill,salesBillObjLst,"");

        }catch (Exception e){
            logger.error("salesBillMerge error:",e);
            return Response.failed("业务单 合并失败:"+e.getMessage());
        }
        return Response.ok("业务单合并成功");
    }

    /**
     * 业务单入池前查询
     * @param poolId
     * @param reqList
     * @param userRole
     * @return
     */
    public Response salesBillPreJoinPool(String poolId, ConditionReqList reqList, Integer userRole) {
        if(reqList == null){
            return Response.failed("请求参数不能为空!");
        }
        checkUserRole(userRole);
        List poolCondition = Lists.newArrayList();
        poolCondition.add(Tables.T_SALES_BILL_POOL.POOL_ID.eq(poolId));
        List<TSalesBillPoolObj> poolObjs = dao.queryObj(Tables.T_SALES_BILL_POOL,poolCondition);
        if (CommonTools.isEmpty(poolObjs)){
            return Response.failed("不存在的结算池ID["+poolId+"]");
        }
        String status = poolObjs.get(0).getStatus();
        if (SalesPoolStatus.SETTLEMENT_GENERATING.value().equals(status)){
            return Response.failed(poolId+"结算池正在计算结算单中，请稍后重试");
        }

        List<String> data = salesBillWithEsService.getSalesBillIds(reqList, SalesBillCatalog.WAITINGFORHANDLE.value(), userRole, LIMIT_COUNT);

        Map map = fillResponseResult(0, Lists.newArrayList(), data.size());
        if (CommonTools.isEmpty(poolId) || CommonTools.isEmpty(data)) {
            return Response.from(Response.Fail,"结算池Id 或 待移入结算池的业务单id集合不能为空！",map);
        }

        List conditions=Lists.newArrayList();
        conditions.add(Tables.T_SALES_BILL.SALES_BILL_ID.in(data));
//        conditions.add(salesBillPermissionTools.getSalesBillPermissionCondition(userRole));
        List<TSalesBillObj> salesBillObjList = create.select(
                Tables.T_SALES_BILL.ID,
                Tables.T_SALES_BILL.SALES_BILL_ID,
                Tables.T_SALES_BILL.STATUS,
                Tables.T_SALES_BILL.SALES_BILL_NO,
                Tables.T_SALES_BILL.POOL_ID,
                Tables.T_SALES_BILL.UPDATE_TIME,
                Tables.T_SALES_BILL.UPLOAD_CONFIRM_FLAG)
                .from(T_SALES_BILL)
                .where(conditions)
                .fetchInto(TSalesBillObj.class);

        if (CommonTools.isEmpty(salesBillObjList)){
            return Response.from(Response.Fail,"查询结果为空！",map);
        }

        if(salesBillObjList.stream().anyMatch(t -> (!CommonTools.isEmpty(t.getPoolId()) && !t.getPoolId().equals(poolId))) ){
            return Response.from(Response.Fail,"不能加入其他人的结算池！",map);
        }

        List<String> result = Lists.newLinkedList();
        List<TSalesBillObj> salesBillObjs = Lists.newLinkedList();
        for (int i = 0; i < salesBillObjList.size(); i++) {
            TSalesBillObj salesBillObj  = salesBillObjList.get(i);
            if (SalesBillCatalog.WAITINGFORHANDLE.value().equals(salesBillObj.getUploadConfirmFlag()) &&
                    (SalesBillStatus.NORMAL.value().equals(salesBillObj.getStatus())) ){
                result.add(salesBillObj.getSalesBillId());
                salesBillObjs.add(salesBillObj);
            }
        }

        map = fillResponseResult(result.size(),salesBillObjs,data.size());
        logger.info("预览业务单入池操作完成");

        return Response.from(Response.OK,"获取成功",map);
    }

    /**
     * 填充response的result结果集
     * @param canTatol
     * @param salesBillObjs
     * @param total
     * @return
     */
    private Map fillResponseResult(int canTatol, List<TSalesBillObj> salesBillObjs,int total){
        Map map = Maps.newHashMap();
        map.put("success",canTatol);
        map.put("salesBillObjs",salesBillObjs);
        map.put("total",total);
        return map;
    }

    @Transactional
    public Response salesBillJoinPool(String poolId, ConditionReqList reqList, Integer userRole) {
        //校验业务单
        Response response = salesBillPreJoinPool(poolId, reqList, userRole);
        if (Response.Fail.equals(response.getCode())){
            return response;
        }
        Map result = (Map) response.getResult();

        try {
            List<TSalesBillObj> salesBillObjList = (List<TSalesBillObj>) result.get("salesBillObjs");
           if (CommonTools.isEmpty(salesBillObjList)){
                throw new SalesbillHandleException("业务单加入结算池失败:查询结果为空");
            }
            List<TSalesBillObj> newTSalesBillObjs = Lists.newLinkedList();
            List<Long> updateIds = Lists.newLinkedList();
            String userId = contextService.currentUser();
            String userName = contextService.currentUserDisplayName();
            for (int i = 0; i < salesBillObjList.size(); i++) {
                TSalesBillObj salesBillObj = salesBillObjList.get(i);
                updateIds.add(salesBillObj.getId());
                salesBillObj.setPoolId(poolId);
                TSalesBillObj newSalesBillObj = new TSalesBillObj();
                newSalesBillObj.setId(salesBillObj.getId());
                newSalesBillObj.setSalesBillId(salesBillObj.getSalesBillId());
                newSalesBillObj.setUserId(userId);
                newSalesBillObj.setUserName(userName);
                newSalesBillObj.setStatus(SalesBillStatus.NORMAL.value());
                newSalesBillObj.setUploadConfirmFlag(SalesBillCatalog.ALREADY_JOIN_POLL.value());
                newSalesBillObj.setReceiveConfirmFlag(SalesBillCatalog.ALREADY_JOIN_POLL.value());
                newSalesBillObj.setComputeStatus(SalesBillComputeStatus.PENDING_COMPUTE.value());
                newSalesBillObj.setAddPoolType(SalesBillAddPoolType.ADD_2_POOL.value());
                newSalesBillObj.setUpdateTime(DateTools.getTime17());
                newSalesBillObj.setPoolId(poolId);
                newTSalesBillObjs.add(newSalesBillObj);
//                historyTools.recordHistoryAfterCommit(SalesBillAction.JOIN_POLL, salesBillObj, newSalesBillObj,"");
            }

            //更新业务单
            updateBatch(Tables.T_SALES_BILL,newTSalesBillObjs);
            //更新业务单记录状态后，将结算池状态更新为待计算状态
            salesBillPoolService.updatePoolStatusForCompute(poolId);

            //更新完成后再同步ES,防止数据库更新失败，ES同步的情况
            historyTools.recordHistoryJoinPoll(newTSalesBillObjs);

//            //记录更新时间
//            salesBillPollTools.updateSalesBillPoolLastTime(poolId,contextService.currentUser());
        }catch (Exception e){
            logger.error("salesBillJoinPool error:",e);
            throw e;
//            return Response.failed("业务单加入结算池失败:"+e.getMessage());
        }
        result.remove("salesBillObjs");
//        sendSalesBillPoolJobMsg();
        logger.info("业务单加入结算单池[{}]完成。",poolId);

        return Response.from(Response.OK,"业务单加入结算池成功",result);

    }

    public Response removeSalesBillFromPool(String poolId,ConditionReqList reqList, Integer userRole) {
        Response removeResp = Response.ok("结算池移除业务单成功");
        try {
            if(reqList == null){
                return Response.failed("请求参数不能为空!");
            }
            checkUserRole(userRole);
            String errTitle = "结算池移除业务单失败:";
            List<String> data = salesBillWithEsService.getSalesBillIds(reqList, SalesBillCatalog.ALREADY_JOIN_POLL.value(), userRole, LIMIT_COUNT);
            if (CommonTools.isEmpty(data) || CommonTools.isEmpty(poolId)) {
                throw new SalesbillHandleException(errTitle+"数据格式非法");
            }

            String removeStr = removeSalesBillFlag(data, userRole);
            if(!CommonTools.isEmpty(removeStr)){
                throw new SalesbillHandleException(removeStr);
            }
            List conditions=Lists.newArrayList();
            conditions.add(Tables.T_SALES_BILL.SALES_BILL_ID.in(data));
//            conditions.add(salesBillPermissionTools.getSalesBillPermissionCondition(userRole));
            List<TSalesBillObj> salesBillObjList = create.select(
                    Tables.T_SALES_BILL.ID,
                    Tables.T_SALES_BILL.SALES_BILL_ID,
                    Tables.T_SALES_BILL.STATUS,
                    Tables.T_SALES_BILL.COMPUTE_STATUS,
                    Tables.T_SALES_BILL.USER_ID,
                    Tables.T_SALES_BILL.SALES_BILL_NO,
                    Tables.T_SALES_BILL.POOL_ID,
                    Tables.T_SALES_BILL.UPDATE_TIME,
                    Tables.T_SALES_BILL.UPLOAD_CONFIRM_FLAG)
                    .from(T_SALES_BILL)
                    .where(conditions)
                    .fetchInto(TSalesBillObj.class);

            if (CommonTools.isEmpty(salesBillObjList)){
                throw new SalesbillHandleException(errTitle+"查询结果为空");
            }

            Response response = handleSalesBillRemovePool(poolId, salesBillObjList);
            if (!Response.OK.equals(response.getCode())) {
                return response;
            } else {
                removeResp.setResult(response.getResult());
            }
        } catch (Exception e){
            logger.error("removeSalesBillFromPool error:",e);
            return Response.failed("结算池移除业务单失败:"+e.getMessage());
        }
        return removeResp;
    }

    /**
     * 特殊场景校验：业务单1：100；业务单2：10；业务单3：-50，最后会合并为一张：60
     * 移除业务单1：100，变成：-40（不符合场景）
     * 校验主题：预制结算单明细
     * @param data
     * @return
     */
    public String removeSalesBillFlag(List<String> data, Integer userRole){
        String removeStr = "";
        try{
            List<String> settlementItemIds = create.selectDistinct(Tables.T_SALES_BILL.SETTLEMENT_ITEM_ID).from(Tables.T_SALES_BILL)
                    .where(Tables.T_SALES_BILL.SALES_BILL_ID.in(data))
                    .fetchInto(String.class);

            if (!CommonTools.isEmpty(settlementItemIds)) {
                Map<String, List<TSalesBillObj>> positiveItemId2ListsMap = new ConcurrentHashMap<>();
                Map<String, List<TSalesBillObj>> unPositiveItemId2ListsMap = new ConcurrentHashMap<>();
                getItemId2ListsMap(settlementItemIds, positiveItemId2ListsMap, unPositiveItemId2ListsMap);
                for(String settlementItem:settlementItemIds){
                    if(CommonTools.isEmpty(settlementItem)){
                        continue;
                    }
                    List<TSalesBillObj> unPositiveList = unPositiveItemId2ListsMap.get(settlementItem);
                    List<TSalesBillObj> positiveList = positiveItemId2ListsMap.get(settlementItem);
                    //既有正又有负校验，其他不校验
                    if(!CommonTools.isEmpty(unPositiveList) && !CommonTools.isEmpty(positiveList)){
                        //获取使用的规则
                        //获取结算单
                        String settlementId = create.select(Tables.T_SALES_BILL_POOL_PREVIEW_ITEM.SETTLEMENT_ID)
                                .from(Tables.T_SALES_BILL_POOL_PREVIEW_ITEM)
                                .where(Tables.T_SALES_BILL_POOL_PREVIEW_ITEM.SETTLEMENT_ITEM_ID.eq(settlementItem))
                                .fetchOneInto(String.class);
                        if(!CommonTools.isEmpty(settlementId)){
                            TSalesBillPoolPreviewObj salesBillPoolPreviewObj = create.select(Tables.T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY,
                                    Tables.T_SALES_BILL_POOL_PREVIEW.POOL_ID)
                                    .from(Tables.T_SALES_BILL_POOL_PREVIEW)
                                    .where(Tables.T_SALES_BILL_POOL_PREVIEW.SETTLEMENT_ID.eq(settlementId))
                                    .fetchOneInto(TSalesBillPoolPreviewObj.class);
                            if(salesBillPoolPreviewObj != null){
                                int discount = create.select(T_SALES_BILL_MERGE_SPLIT_RULE.DISCOUNT)
                                        .from(T_SALES_BILL_MERGE_SPLIT_RULE)
                                        .where(T_SALES_BILL_MERGE_SPLIT_RULE.SELLER_PURCHASER_HASH_KEY.eq(salesBillPoolPreviewObj.getSellerPurchaserHasyKey()))
                                        .and(T_SALES_BILL_MERGE_SPLIT_RULE.POOL_ID.eq(salesBillPoolPreviewObj.getPoolId()))
                                        .and(T_SALES_BILL_MERGE_SPLIT_RULE.RULE_TYPE.eq(0))
                                        .limit(1)
                                        .fetchOne(T_SALES_BILL_MERGE_SPLIT_RULE.DISCOUNT);
                                if(discount == SalesBillDiscountType.YES.value()){//合并为折扣项
//                                    List<String> dataStr = create.selectDistinct(Tables.T_SALES_BILL.SALES_BILL_ID).from(Tables.T_SALES_BILL)
//                                            .where(Tables.T_SALES_BILL.SETTLEMENT_ITEM_ID.eq(settlementItem))
//                                            .fetchInto(String.class);
//                                    data.addAll(dataStr);
                                } else {//正负直接合并
                                    positiveList.addAll(unPositiveList);
                                    int settlementSalesCount = create.selectCount().from(Tables.T_SALES_BILL)
                                            .where(Tables.T_SALES_BILL.SALES_BILL_ID.in(data)
                                            .and(T_SALES_BILL.SETTLEMENT_ITEM_ID.eq(settlementItem))
                                            .and(T_SALES_BILL.STATUS.ne(SalesBillStatus.DELETED.value())))
                                            .fetchOne().value1();

                                    if(settlementSalesCount != positiveList.size()){//结算单明细全部移除不做判断

                                        BigDecimal amountWithTax = BigDecimal.ZERO;
                                        BigDecimal unPositiveAmount = BigDecimal.ZERO;

                                        BigDecimal discountAmount = BigDecimal.ZERO;
                                        BigDecimal unDiscountAmount = BigDecimal.ZERO;

                                        BigDecimal deduction = BigDecimal.ZERO;
                                        BigDecimal unDeduction = BigDecimal.ZERO;

                                        StringBuilder builder = new StringBuilder();
                                        for(TSalesBillObj salesBill:positiveList){
                                            if(data.stream().anyMatch(t -> salesBill.getSalesBillId().equals(t))){
                                                builder.append(salesBill.getSalesBillNo()+";");
                                                continue;//跳过本次移除的业务单
                                            }
                                            if(salesBill.getAmountWithTax().compareTo(BigDecimal.ZERO) ==1) {
                                                amountWithTax = amountWithTax.add(salesBill.getAmountWithTax());
                                                discountAmount = discountAmount.add(salesBill.getDiscountWithTax());
                                                deduction = deduction.add(salesBill.getDeduction());
                                            }else{
                                                unPositiveAmount = unPositiveAmount.add(salesBill.getAmountWithTax());
                                                unDiscountAmount = unDiscountAmount.add(salesBill.getDiscountWithTax());
                                                unDeduction = unDeduction.add(salesBill.getDeduction());
                                            }
                                        }

                                    //查询结算单编号
                                        if ((amountWithTax.compareTo(unPositiveAmount.abs()) > 0) &&
                                                (discountAmount.compareTo(unDiscountAmount.abs()) >= 0) &&
                                                (deduction.compareTo(unDeduction.abs()) >= 0) &&
                                                ((amountWithTax.add(unPositiveAmount)).subtract(discountAmount.add(unDiscountAmount)).subtract(deduction.add(unDeduction)).compareTo(BigDecimal.ZERO) > 0)  ){

                                        } else {
                                            logger.info("removeSalesBillFlag不满足移除条件：amountWithTax:{}=unPositiveAmount:{}=discountAmount:{}=unDiscountAmount:{}=deduction:{}=unDeduction:{}",amountWithTax,unPositiveAmount,discountAmount,unDiscountAmount,deduction,unDeduction);
                                            removeStr = "移除业务单操作有误,【业务单编号:"+builder.toString()+",已经合并生成预制结算编号:"+salesBillPoolPreviewObj.getSettlementNo()+"】,移除会导致结算单金额有误,请至结算池操作移除!";
                                        }
                                    }

                                }

                            }

                        }
                    }
                }
            }
        }catch(Exception e){
            logger.error("移除校验失败："+e.getMessage(),e);
            removeStr = "移除失败";
        }

        return removeStr;
    }

    private void getItemId2ListsMap(List<String> settlementItemIds,
                                        Map<String, List<TSalesBillObj>> positiveItemId2ListsMap,
                                        Map<String, List<TSalesBillObj>> unPositiveItemId2ListsMap) {
        List<TSalesBillObj> salesBillLists = create.select(Tables.T_SALES_BILL.SALES_BILL_ID,
                Tables.T_SALES_BILL.SALES_BILL_NO,Tables.T_SALES_BILL.AMOUNT_WITH_TAX,
                Tables.T_SALES_BILL.DISCOUNT_WITH_TAX,Tables.T_SALES_BILL.DEDUCTION,
                Tables.T_SALES_BILL.SETTLEMENT_ITEM_ID
        ).from(T_SALES_BILL)
                .where(T_SALES_BILL.SETTLEMENT_ITEM_ID.in(settlementItemIds)
                        .and(T_SALES_BILL.STATUS.ne(SalesBillStatus.DELETED.value())))
                .fetchInto(TSalesBillObj.class);
        salesBillLists.stream().forEach(item ->{
                    // 取正数业务单
                    if(item.getAmountWithTax().compareTo(BigDecimal.ZERO) > 0){
                        List<TSalesBillObj> positiveList = positiveItemId2ListsMap.get(item.getSettlementItemId());
                        if (positiveList == null) {
                            positiveList  = Lists.newArrayList();
                        }
                        positiveList.add(item);
                        positiveItemId2ListsMap.put(item.getSettlementItemId(),positiveList);
                    }else{
                        // 取负数业务单
                        List<TSalesBillObj> unPositiveList = unPositiveItemId2ListsMap.get(item.getSettlementItemId());
                        if (unPositiveList == null) {
                            unPositiveList  = Lists.newArrayList();
                        }
                        unPositiveList.add(item);
                        unPositiveItemId2ListsMap.put(item.getSettlementItemId(),unPositiveList);
                    }
                }
        );
    }

    public Response removeSettlementFromPool(String settlementId, Integer userRole) {
        String errTitle = "结算池移除预览结算单失败:";
        Response removeResp = Response.ok("结算池移除结算单成功");
        try {
            checkUserRole(userRole);
            if (StringUtils.isEmpty(settlementId)){
                return Response.failed(errTitle+"数据格式非法");
            }
            List conditions=Lists.newArrayList();
            conditions.add(Tables.T_SALES_BILL.SETTLEMENT_ID.eq(settlementId));
            conditions.add(salesBillPermissionTools.getSalesBillPermissionCondition(userRole));
//            List<TSalesBillObj> salesBillObjList = dao.queryObj(Tables.T_SALES_BILL,conditions);
            List<TSalesBillObj> salesBillObjList = create.select()
                    .from(Tables.T_SALES_BILL)
                    .where(conditions)
                    .fetchInto(TSalesBillObj.class);

            if (salesBillObjList.size()==0){
                return Response.failed(errTitle+"查询结果为空");
            }
            String poolId = salesBillObjList.get(0).getPoolId();
            Response response = handleSalesBillRemovePool(poolId,salesBillObjList);
            if (!Response.OK.equals(response.getCode())) {
                return response;
            } else {
                removeResp.setResult(response.getResult());
            }
        }catch (Exception e){
            logger.error("removeSettlementFromPool error:",e);
            return Response.failed(errTitle+e.getMessage());
        }
        return removeResp;
    }

    public Response removeSettlementItemFromPool(String settlementItemId, Integer userRole) {
        String errTitle = "结算池移除结算单预览明细失败:";
        Response removeResp = Response.ok("结算池移除结算单明细成功");
        try {
            checkUserRole(userRole);
            if (StringUtils.isEmpty(settlementItemId)){
                return Response.failed(errTitle+"数据格式非法");
            }
            List conditions=Lists.newArrayList();
            conditions.add(Tables.T_SALES_BILL.SETTLEMENT_ITEM_ID.in(settlementItemId));
            conditions.add(salesBillPermissionTools.getSalesBillPermissionCondition(userRole));
            List<TSalesBillObj> salesBillObjList = create.select()
                    .from(Tables.T_SALES_BILL)
                    .where(conditions)
                    .fetchInto(TSalesBillObj.class);
            if (salesBillObjList.size()==0){
                return Response.failed(errTitle+"查询结果为空");
            }
            String poolId = salesBillObjList.get(0).getPoolId();
            Response response = handleSalesBillRemovePool(poolId,salesBillObjList);
            if (!Response.OK.equals(response.getCode())) {
                return response;
            } else {
                removeResp.setResult(response.getResult());
            }
        }catch (Exception e){
            logger.error("removeSettlementItemFromPool error:",e);
            return Response.failed(errTitle+e.getMessage());
        }
        return removeResp;
    }

    /**
     * 处理结算单出池操作
     * @param poolId
     * @param salesBillObjList
     * @return
     */
//    @XPlatTrace(operationName = "处理出池操作")
//    @Transactional
    public Response handleSalesBillRemovePool(String poolId, List<TSalesBillObj> salesBillObjList) {
        logger.info("处理移除业务单操作开始，结算池[{}]",poolId);
        int success = 0;
        try {
            checkRemoveSalesBillFromPoolPermission(salesBillObjList);
            checkSalesBillPoolStatus(poolId);
            StringBuilder salesBillFailWaringMsgBuilder = new StringBuilder();
            List<TSalesBillObj> newTSalesBillObjs = Lists.newLinkedList();
            //4-待处理 5-已加入结算池 6-已生成结算单 9-删除 0-作废
            for (int i = 0; i < salesBillObjList.size(); i++) {
                TSalesBillObj salesBillObj = salesBillObjList.get(i);
                //作废、已完成（已生成结算单）、逻辑删除
                if (SalesBillStatus.INVALID.value().equals(salesBillObj.getStatus())
    //                   || SalesBillStatus.FINISHED.value().equals(salesBillObj.getStatus())
                        || !SalesBillCatalog.ALREADY_JOIN_POLL.value().equals(salesBillObj.getUploadConfirmFlag())
                        || SalesBillStatus.DELETED.value().equals(salesBillObj.getStatus())) {
                    salesBillFailWaringMsgBuilder.append("业务单NO:"+salesBillObj.getSalesBillNo()+"状态:"+SalesBillStatus.fromValue(salesBillObj.getStatus()).getDesc() +"不做处理\n");
                    continue;
                }
                TSalesBillObj newTSalesBillObj = new TSalesBillObj();
                newTSalesBillObj.setId(salesBillObj.getId());
                newTSalesBillObj.setSalesBillId(salesBillObj.getSalesBillId());
                newTSalesBillObj.setUserId(CommonTools.emptyString());
                newTSalesBillObj.setUserName(CommonTools.emptyString());
                newTSalesBillObj.setStatus(SalesBillStatus.NORMAL.value());
                newTSalesBillObj.setUploadConfirmFlag(SalesBillCatalog.WAITINGFORHANDLE.value());
                newTSalesBillObj.setReceiveConfirmFlag(SalesBillCatalog.WAITINGFORHANDLE.value());
                newTSalesBillObj.setPoolId(CommonTools.emptyString());
                // 业务单入池未计算
                if(SalesBillComputeStatus.PENDING_COMPUTE.value().equals(salesBillObj.getComputeStatus())){
                    newTSalesBillObj.setComputeStatus(SalesBillComputeStatus.NONE.value());
                    newTSalesBillObj.setAddPoolType(SalesBillAddPoolType.NONE.value());
                }else {// 业务单入池计算中，计算完成
                    newTSalesBillObj.setComputeStatus(SalesBillComputeStatus.PENDING_COMPUTE.value());
                    newTSalesBillObj.setAddPoolType(SalesBillAddPoolType.REMOVE_2_P00L.value());
                }
                success++;
                newTSalesBillObjs.add(newTSalesBillObj);
            }
            //更新业务单
            updateBatch(Tables.T_SALES_BILL,newTSalesBillObjs);
            //更新业务单记录状态后，将结算池状态更新为待计算状态
            salesBillPoolService.updatePoolStatusForCompute(poolId);

            historyTools.recordHistoryRemoveJoinPoll(newTSalesBillObjs);
//            sendSalesBillPoolJobMsg();
//            if(!CommonTools.isEmpty(salesBillFailWaringMsgBuilder.toString())){
//                return Response.ok("结算池移除相关业务单部分成功，部分失败原因：【"+salesBillFailWaringMsgBuilder.toString()+"】");
//            }
//            checkSalesBillPoolStatus(poolId);
        } catch (Exception e) {
            logger.info("handleSalesBillRemovePool err",e);
            throw e;
        }
        logger.info("处理移除业务单操作完成，结算池[{}]",poolId);

        Map map = Maps.newHashMap();
        map.put("success",success);
        map.put("total",salesBillObjList.size());

        return Response.from(Response.OK,"处理移除业务单操作完成",map);
    }

    /**
     * 检查结算单池状态是否可用
     * @param poolId
     */
    private void checkSalesBillPoolStatus(String poolId){
        List poolCondition= Lists.newArrayList();
        poolCondition.add(Tables.T_SALES_BILL_POOL.POOL_ID.eq(poolId));
        List<TSalesBillPoolObj> poolLst = dao.queryObj(Tables.T_SALES_BILL_POOL,poolCondition);
        if (org.springframework.util.CollectionUtils.isEmpty(poolLst)){
            throw new  SalesbillHandleException("结算池集合为空");
        }
        String status = poolLst.get(0).getStatus();
        if (SalesPoolStatus.COMPUTING.value().equals(status)){
            throw new  SalesbillHandleException("结算池正在计算中，请稍后重试");
        }

        if (!SalesPoolStatus.availableRemoveSalesBillStatusValue().contains(status)){
            throw new  SalesbillHandleException("结算池状态为【"+status+"】不支持移除业务单，请稍后重试");
        }
    }

    /**
     * 检查是否有移除业务单的权限
     * @param salesBillObjLst 移除业务单对象集合
     */
    private void checkRemoveSalesBillFromPoolPermission(List<TSalesBillObj> salesBillObjLst){
        String currentUserId = contextService.currentUser();
        for(TSalesBillObj  salesBillObj:salesBillObjLst){
            if(!currentUserId.equals(salesBillObj.getUserId())){
                throw new RuntimeException("业务单状态非正常状态，不能移除");
            }
        }
    }

    public List<SalesBillPool> salesBillGetPool( Integer userRole) {
        try {
            String userId = contextService.currentUser();
            salesBillPollTools.getPoolByUserId(userId);
            List conditions=Lists.newArrayList();
            conditions.add(Tables.T_SALES_BILL_POOL.USER_ID.eq(userId));
            List<TSalesBillPoolObj> list = dao.queryObj(Tables.T_SALES_BILL_POOL,conditions);
            List<SalesBillPool>  result = new ArrayList<SalesBillPool>();
            for (int i = 0; i < list.size(); i++) {
                SalesBillPool pool = new SalesBillPool();
                BeanUtils.copy(list.get(i),pool);
                result.add(pool);
            }
            return result;
        }catch (Exception e){
            logger.error("salesBillGetPool error",e);
        }
        return null;
    }

    public SalesBillPool salesBillGetPoolById(String poolId, ConditionPoolRequest data) {
        SalesBillPool salesBillPool = new SalesBillPool();
        salesBillPool.settlementNumber(0);
        salesBillPool.setSalesBillNumber(0);
        salesBillPool.setAmountWithoutTax("0");
        salesBillPool.setTaxAmount("0");
        salesBillPool.setAmountWithtax("0");
        salesBillPool.setDeduction("0");
        salesBillPool.setDiscountWithTax("0");
        salesBillPool.setDiscountTax("0");
        salesBillPool.setDiscountWithoutTax("0");
        if (StringUtils.isEmpty(poolId)){
            return salesBillPool;
        }
        List tmp=Lists.newArrayList();
        tmp.add(Tables.T_SALES_BILL_POOL.POOL_ID.eq(poolId));
        List<TSalesBillPoolObj> list = dao.queryObj(Tables.T_SALES_BILL_POOL, tmp);
        if (CollectionUtils.isEmpty(list)){
            return salesBillPool;
        }
        salesBillPool.setPoolId(list.get(0).getPoolId());
        salesBillPool.setUserId(list.get(0).getUserId());
        salesBillPool.setTenantCode(list.get(0).getTenantCode());
        salesBillPool.setCompanyCode(list.get(0).getCompanyCode());
        salesBillPool.setExtColumnCode(list.get(0).getExtColumnCode());
        salesBillPool.setExtColumnName(list.get(0).getExtColumnName());
        salesBillPool.setPoolType(list.get(0).getPoolType());
        salesBillPool.setStatus(list.get(0).getStatus());
        salesBillPool.setCreateTime(list.get(0).getCreateTime());
        salesBillPool.setCreateUserId(list.get(0).getCreateUserId());
        salesBillPool.setUpdateTime(list.get(0).getUpdateTime());
        salesBillPool.setUpdateUserId(list.get(0).getUpdateUserId());
        Condition conditions = T_SALES_BILL_POOL_PREVIEW.POOL_ID.eq(poolId).and(T_SALES_BILL_POOL_PREVIEW.STATUS.eq(SettlementStatus.NORMAL.value()));
        if(data.getSellerCompanyInfos() != null && !data.getSellerCompanyInfos().isEmpty()) {
            List<CompanyInfo> companies = data.getSellerCompanyInfos();
            Set<String> codes = companies.stream().filter(t -> !StringUtils.isEmpty(t.getCode())).map(t -> t.getCode()).collect(Collectors.toSet());
            Set<String> names = companies.stream().filter(t -> !StringUtils.isEmpty(t.getName())).map(t -> t.getName()).collect(Collectors.toSet());
            Set<String> taxNos = companies.stream().filter(t -> !StringUtils.isEmpty(t.getTaxNo())).map(t -> t.getTaxNo()).collect(Collectors.toSet());
            conditions = conditions.and(T_SALES_BILL_POOL_PREVIEW.SELLER_CODE.in(codes).or(T_SALES_BILL_POOL_PREVIEW.SELLER_NAME.in(names)).or(T_SALES_BILL_POOL_PREVIEW.SELLER_TAX_NO.in(taxNos)));
        }
        if(data.getPurchaserCompanyInfos() != null && !data.getPurchaserCompanyInfos().isEmpty()) {
            List<CompanyInfo> companies = data.getPurchaserCompanyInfos();
            Set<String> codes = companies.stream().filter(t -> !StringUtils.isEmpty(t.getCode())).map(t -> t.getCode()).collect(Collectors.toSet());
            Set<String> names = companies.stream().filter(t -> !StringUtils.isEmpty(t.getName())).map(t -> t.getName()).collect(Collectors.toSet());
            Set<String> taxNos = companies.stream().filter(t -> !StringUtils.isEmpty(t.getTaxNo())).map(t -> t.getTaxNo()).collect(Collectors.toSet());
            conditions = conditions.and(T_SALES_BILL_POOL_PREVIEW.PURCHASER_CODE.in(codes).or(T_SALES_BILL_POOL_PREVIEW.PURCHASER_NAME.in(names)).or(T_SALES_BILL_POOL_PREVIEW.PURCHASER_TAX_NO.in(taxNos)));
        }
        StringBuilder having = new StringBuilder();
        having.append("true");
        if(data.getMinTaxAmount() != null){
            having.append(" and taxAmount - discountTaxAmount >= "+data.getMinTaxAmount());
        }
        if(data.getMaxTaxAmount() != null){
            having.append(" and taxAmount - discountTaxAmount <= "+data.getMaxTaxAmount());
        }
        if(data.getMinAmountWithoutTax() != null){
            having.append(" and amountWithoutTax - discountAmountWithoutTax >= "+data.getMinAmountWithoutTax());
        }
        if(data.getMaxAmountWithoutTax() != null){
            having.append(" and amountWithoutTax - discountAmountWithoutTax <= "+data.getMaxAmountWithoutTax());
        }
        if(data.getMinAmountWithTax() != null){
            having.append(" and amountWithTax - discountAmountWithTax >= "+data.getMinAmountWithTax());
        }
        if(data.getMaxAmountWithTax() != null){
            having.append(" and amountWithTax - discountAmountWithTax <= "+data.getMaxAmountWithTax());
        }
        //
        List<PoolReview> poolReviews = create.select(
                T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY,
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.AMOUNT_WITH_TAX,BigDecimal.ZERO)).as("amountWithTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.AMOUNT_WITHOUT_TAX,BigDecimal.ZERO)).as("amountWithoutTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.TAX_AMOUNT,BigDecimal.ZERO)).as("taxAmount"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_WITHOUT_TAX,BigDecimal.ZERO)).as("discountAmountWithoutTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_TAX,BigDecimal.ZERO)).as("discountTaxAmount"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DISCOUNT_WITH_TAX,BigDecimal.ZERO)).as("discountAmountWithTax"),
                DSL.sum(DSL.isnull(T_SALES_BILL_POOL_PREVIEW.DEDUCTION,BigDecimal.ZERO)).as("deductions"))
                .from(T_SALES_BILL_POOL_PREVIEW)
                .where(conditions)
                .groupBy(T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY)
                .having(having.toString())
                .fetchInto(PoolReview.class);
        //settlementCount
        Set<String> sellerPurchaserHasyKeys = poolReviews.stream().map(t -> t.getSellerPurchaserHasyKey()).collect(Collectors.toSet());
        List<String> settlementIds = create.select(Tables.T_SALES_BILL_POOL_PREVIEW.SETTLEMENT_ID)
                .from(T_SALES_BILL_POOL_PREVIEW)
                .where(T_SALES_BILL_POOL_PREVIEW.POOL_ID.eq(poolId).and(T_SALES_BILL_POOL_PREVIEW.STATUS.eq(SettlementStatus.NORMAL.value())))
                        .and(T_SALES_BILL_POOL_PREVIEW.SELLER_PURCHASER_HASY_KEY.in(sellerPurchaserHasyKeys))
                .fetchInto(String.class);
        salesBillPool.setSettlementNumber(settlementIds.size());
        //salesBillCount
        String[] settlementIdArray = new String[settlementIds.size()];
        settlementIds.toArray(settlementIdArray);
        int salesBillCount = salesBillTools.getSalesBillListCount(SalesBillCatalog.ALREADY_JOIN_POLL.value(),1,settlementIdArray);
        salesBillPool.setSalesBillNumber(salesBillCount);
        //amount,discount,deduction
        BigDecimal amountWithoutTax = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal amountWithTax = BigDecimal.ZERO;
        BigDecimal discountWithoutTax = BigDecimal.ZERO;
        BigDecimal discountTax = BigDecimal.ZERO;
        BigDecimal discountWithTax = BigDecimal.ZERO;
        BigDecimal deductions = BigDecimal.ZERO;
        for (PoolReview review : poolReviews) {
            amountWithoutTax = amountWithoutTax.add(new BigDecimal(review.getAmountWithoutTax()));
            taxAmount = taxAmount.add(new BigDecimal(review.getTaxAmount()));
            amountWithTax = amountWithTax.add(new BigDecimal(review.getAmountWithTax()));
            discountWithoutTax  = discountWithoutTax.add(new BigDecimal(review.getDiscountAmountWithoutTax()));
            discountTax = discountTax.add(new BigDecimal(review.getDiscountTaxAmount()));
            discountWithTax = discountWithTax.add(new BigDecimal(review.getDiscountAmountWithTax()));
            deductions = deductions.add(new BigDecimal(review.getDeductions()));
        }
//        a.汇总【不含税金额】 = Round(不含税金额 - 不含税折扣金额, 2)
//        b.汇总【税额】 = Round(税额 - 折扣税额, 2)
//        c.汇总【含税金额】 = Round(含税金额 - 含税折扣金额, 2)
        salesBillPool.setAmountWithoutTax(amountWithoutTax.subtract(discountWithoutTax).toPlainString());
        salesBillPool.setTaxAmount(taxAmount.subtract(discountTax).toPlainString());
        salesBillPool.setAmountWithtax(amountWithTax.subtract(discountWithTax).toPlainString());
        salesBillPool.setDiscountWithoutTax(discountWithoutTax.toPlainString());
        salesBillPool.setDiscountTax(discountTax.toPlainString());
        salesBillPool.setDiscountWithTax(discountWithTax.toPlainString());
        salesBillPool.setDeduction(deductions.toPlainString());
        return salesBillPool;
    }

    public Response rollBackSalesBill(SalesBillMergeAndSplitRollback salesBillMergeSplit, Integer userRole) {
        Response response = salesBillMergeAndSplitRollBackService.rollBackSalesBill(salesBillMergeSplit, userRole);
        if(response.getCode() == 1){
            // 业务单回撤需要删除对应记录的数据库与ES
            // salesBillMergeAndSplitRollBackService.rollBackSalesBill(salesBillMergeSplit, userRole);中
            // @XPlatTrace(operationName = "回撤业务单")造成ES删除不稳定,多数无法删除
            List<String> targetSalesBillIdLst = salesBillMergeSplit.getData().stream().distinct().collect(Collectors.toList());
            List<SalesBillConditionRequest> conditions = new LinkedList<>();
            SalesBillConditionRequest condition = new SalesBillConditionRequest();
            List<RequestField> requestFields = new LinkedList<>();
            RequestField requestField = new RequestField();
            requestField.setQueryType(Integer.valueOf(5));//5枚举复选
            requestField.setName("salesBillId");
            requestField.setEnumValue(targetSalesBillIdLst);
            requestFields.add(requestField);
            condition.setRequestFields(requestFields);
            conditions.add(condition);
            String catalog = "4";// 4-待处理、5-已加入结算池、6-已完成、0-已作废、1-全部
            salesBillEsTools.deleteSalesBillEsById(conditions, catalog, userRole, 0, 100);//删除数据库
        }
        return response;
    }

    public Response salesBillSplit(String sourceSalesBillId, String splitType, String splitByMoneyStatus, List<SalesbillItem> salesBillSplitLists, Integer userRole) {
        return salesBillSplitService.splitSalesBillSplit(sourceSalesBillId, splitType, splitByMoneyStatus, salesBillSplitLists, userRole);
    }

    public Response salesBillSplitPreview(String sourceSalesBillId, String splitType, String splitByMoneyStatus, List<SalesbillItem> salesBillSplitLists, Integer userRole) {
        return salesBillSplitService.splitSalesBillSplitPreview(sourceSalesBillId, splitType, splitByMoneyStatus, salesBillSplitLists, userRole);
    }

    /**
     * 按照筛选条件批量导出业务单
     * @param reqList 前段传入的 筛选条件
     * @param columnsData 要导出业务单列字段
     * @param userRole 用户角色
     * @return
     */
    public Response salesBillExportByCondition(ConditionReqList reqList, List<String> columnsData, Integer userRole) {
        checkUserRole(userRole);
        if (reqList== null || CommonTools.isEmpty(reqList.getCatalog())) {
            return Response.from(Response.Fail,"导出时，请求参数不能为空!");
        }
        //直接导出最大数量限制
        final int directExportMaxNum = 100;
        //mq导出最大数量限制
        final int mqExportMaxNum = 50000;
        //传入参数为null，使用默认导出列
        if(CollectionUtils.isEmpty(columnsData)) {
            columnsData = Arrays.asList(defaultSalesBillHeader);
        }

        //3.需要导出的业务单列
        List<String> salesBillIds = salesBillWithEsService.getSalesBillIds(reqList, reqList.getCatalog(), userRole, mqExportMaxNum);
        if(salesBillIds.size() > mqExportMaxNum) {
            return Response.from(Response.Fail,"一次导出的业务单数量不能超过" + mqExportMaxNum + "条,请重新设置条件!");
        }

        List<String> salesBillHeaderList = salesBillTools.getCNColumnsByFields(columnsData);
        String[] salesBillCNHeader = salesBillHeaderList.toArray(new String[salesBillHeaderList.size()]);
        //直接导出
        if (salesBillIds.size() <= directExportMaxNum) {
            List exportSalesBillColumns = salesBillTools.getTableFieldListByFields(columnsData);
            String[] enSalesBillColumns = columnsData.toArray(new String[columnsData.size()]);
            return salesBillTools.exportSalesBillToExcel(salesBillIds,enSalesBillColumns,salesBillCNHeader,exportSalesBillColumns);
        } else {
            //mq方式，走导出服务器
            String businessId =CommonTools.getUUID();
            String userId = contextService.currentUser();
            String httpPath = salesBillTools.generateSalesBillFilePath(businessId);
            Map map = Maps.newHashMap();
            map.put("size",salesBillIds.size());
            map.put("type","ids");
            map.put("httpPath",httpPath);
            map.put("businessId",businessId);
            map.put("userId",userId);
            map.put("ids",salesBillIds);
            map.put("salesBillCNHeader", salesBillCNHeader);
            map.put("columnsData",columnsData);
            String msg = JsonUtils.writeObjectToFastJson(map);
            int sendMessage = loggedMessageService.innerSendMessage(Queues.SALES_BILL_EXPORT, msg, Maps.newHashMap());
            if (sendMessage>0){
                return Response.ok("您已选择 " + salesBillIds.size() + " 张业务单，系统将在处理完成后给您发送通知");
            }else {
                return Response.failed("业务单处理失败，请重新操作或联系管理员");
            }
        }
    }

    public Response salesBillExportByIds(List<String> ids,List<String> columnsData, Integer userRole) {
        checkUserRole(userRole);
        if (CommonTools.isEmpty(ids)) {
            return Response.from(Response.Fail,"导出时，选择的业务单记录不能为空!");
        }
        //直接导出最大数量限制
        final int directExportMaxNum = 100;
        //mq导出最大数量限制
        final int mqExportMaxNum = 50000;
        //传入参数为null，使用默认导出列
        if(CollectionUtils.isEmpty(columnsData)) {
            columnsData = Arrays.asList(defaultSalesBillHeader);
        }
        //1.组合筛选条件
        Condition condition=Tables.T_SALES_BILL.SALES_BILL_ID.in(ids);
        //2.当前角色是否有权限导出这些列
        condition=condition.and(salesBillPermissionTools.getSalesBillPermissionCondition(userRole));
        if(ids.size()> mqExportMaxNum) {
            return Response.from(Response.Fail,"一次导出的业务单数量不能超过" + mqExportMaxNum + "条,请重新设置条件!");
        }

        //3.需要导出的业务单列
        List exportSalesBillColumns = salesBillTools.getTableFieldListByFields(columnsData);
        List<String> salesBillHeaderList = salesBillTools.getCNColumnsByFields(columnsData);
        String[] salesBillCNHeader = salesBillHeaderList.toArray(new String[salesBillHeaderList.size()]);
        String[] enSalesBillColumns = columnsData.toArray(new String[columnsData.size()]);
        if (ids.size()<= directExportMaxNum) {//直接导出
            return salesBillTools.exportSalesBillToExcel(ids,enSalesBillColumns,salesBillCNHeader,exportSalesBillColumns);
        } else { //mq方式，走导出服务器
            String businessId =CommonTools.getUUID();
            String userId = contextService.currentUser();
            String httpPath = salesBillTools.generateSalesBillFilePath(businessId);
            Map map = Maps.newHashMap();
            map.put("size",ids.size());
            map.put("type","ids");
            map.put("httpPath",httpPath);
            map.put("businessId",businessId);
            map.put("userId",userId);
            map.put("ids",ids);
            map.put("salesBillCNHeader", salesBillCNHeader);
            map.put("exportSalesBillColumns", exportSalesBillColumns);
            map.put("enSalesBillColumns", enSalesBillColumns);
            String msg = JsonUtils.writeObjectToFastJson(map);
            int sendMessage = loggedMessageService.innerSendMessage(Queues.SALES_BILL_EXPORT, msg, Maps.newHashMap());
            if (sendMessage>0){
                return Response.ok("您已选择 " + ids.size() + " 张业务单，系统将在处理完成后给您发送通知");
            }else {
                return Response.failed("业务单处理失败，请重新操作或联系管理员");
            }
        }
    }

    /**
     * 下载业务单excel
     * @param key
     * @return
     */
    public Response downloadSalesBill(String key) {
        String url = FileDownloadController.downloadUrl+"?key="+key;
        return Response.from(Response.OK, "获取url成功！", url);
    }

    /**
     * 填充规则必要字段
     * @param ruleObj  规则对象
     * @param sellerPurchaserHashKey  购销公司 hashkey
     */
    private void fulfillSalesBillSplitRuleObj(ConfigList conf, TSalesBillMergeSplitRuleObj ruleObj,
                                              String poolId, String sellerPurchaserHashKey, String flag) {
        CommonTools.copyProperties(conf, ruleObj);
        List<SalesBillConfigDetail> jeRule = conf.getDetailList().stream().filter(a -> a.getName().equals(RuleNameEnum.JEHB.getCode())).collect(Collectors.toList());
        //组合
        if (RuleNameEnum.JSZH.getCode().equals(flag)) {
            List<SalesBillConfigDetail> zhRule = conf.getDetailList().stream().filter(a -> a.getName().equals(RuleNameEnum.JSZH.getCode())).collect(Collectors.toList());
            ruleObj.setRuleName(RuleNameEnum.JSZH.getValue());
            ruleObj.setRuleType(Integer.parseInt(RuleNameEnum.JSZH.getCode()));
            if (!CommonTools.isEmpty(zhRule) && zhRule.get(0) != null) {
                ruleObj.setSeqId(zhRule.get(0).getSalesBillConfigDetailId());
                if (!CommonTools.isEmpty(zhRule.get(0).getValue())) {
                    ruleObj.setDenycompositfields(zhRule.get(0).getValue());
                }
            } else {
                //兼容前面默认规则查询,故取金额合并规则seqID
                ruleObj.setSeqId(jeRule.get(0).getSalesBillConfigDetailId());
            }
        }
        //合并
        if (RuleNameEnum.HBJS.getCode().equals(flag)) {
            List<SalesBillConfigDetail> hbRule = conf.getDetailList().stream().filter(a -> a.getName().equals(RuleNameEnum.HBJS.getCode())).collect(Collectors.toList());
            if (!CommonTools.isEmpty(hbRule) && hbRule.get(0) != null && !CommonTools.isEmpty(hbRule.get(0).getValue())) {
                ruleObj.setAllowmergefields(hbRule.get(0).getValue());
            }
            if (!CommonTools.isEmpty(jeRule) && !CommonTools.isEmpty(jeRule.get(0).getValue())) {
                ruleObj.setSeqId(jeRule.get(0).getSalesBillConfigDetailId());
                ruleObj.setRuleName(RuleNameEnum.HBJS.getValue());
                ruleObj.setRuleType(Integer.parseInt(RuleNameEnum.HBJS.getCode()));
                ruleObj.setQuantityPriceResult(Integer.parseInt(jeRule.get(0).getValue()));//设置价格方式
            }
            List<SalesBillConfigDetail> zfRule = conf.getDetailList().stream().filter(a -> a.getName().equals(RuleNameEnum.DISCOUNT.getCode())).collect(Collectors.toList());
            if (!CommonTools.isEmpty(zfRule) && !CommonTools.isEmpty(zfRule.get(0).getValue())) {
                ruleObj.setDiscount(Integer.parseInt(zfRule.get(0).getValue()));//设置合并负数为折扣行
            }
        }
        //备注
        if (RuleNameEnum.BZPJ.getCode().equals(flag)) {
            List<SalesBillConfigDetail> hbRule = conf.getDetailList().stream().filter(a -> a.getName().equals(RuleNameEnum.BZPJ.getCode())).collect(Collectors.toList());
            if (!CommonTools.isEmpty(hbRule) && hbRule.get(0) != null && !CommonTools.isEmpty(hbRule.get(0).getValue())) {
                ruleObj.setDenycompositfields(hbRule.get(0).getValue());
            }
            List<SalesBillConfigDetail> bzpjRule = conf.getDetailList().stream().filter(a -> a.getName().equals(RuleNameEnum.BZPYN.getCode())).collect(Collectors.toList());

            if (!CommonTools.isEmpty(bzpjRule) && !CommonTools.isEmpty(bzpjRule.get(0).getValue())) {
                ruleObj.setSeqId(bzpjRule.get(0).getSalesBillConfigDetailId());
                ruleObj.setRuleName(RuleNameEnum.BZPJ.getValue());
                ruleObj.setRuleType(Integer.parseInt(RuleNameEnum.BZPJ.getCode()));
                ruleObj.setDiscount(Integer.parseInt(bzpjRule.get(0).getValue()));//设置合并负数为折扣行
            }
        }
        //填充默认
        String userId = contextService.currentUser();
        String userName = contextService.currentUserDisplayName();
        ruleObj.setPoolId(poolId);
        ruleObj.setSellerPurchaserHashKey(sellerPurchaserHashKey);
        ruleObj.setAllowMerge(1);
        ruleObj.setMergeLimit("1");
        ruleObj.setStatus("1");
        ruleObj.setPriorityLevel(1);
        ruleObj.setUserId(userId);
        ruleObj.setUserName(userName);
        ruleObj.setCreateTime(DateTools.getTime17());
        ruleObj.setUpdateTime(DateTools.getTime17());
        ruleObj.setCreateUserId(userId);
        ruleObj.setUpdateUserId(userId);
    }

    /**
     * 根据结算池Id、结算单Id集合，查询结算单
     * @param poolId
     * @param settlementIdLst
     * @return  业务单Id集合
     */
    private List<TSalesBillObj> getPreRemoveSalesBillObjLst(String poolId, List<String> settlementIdLst) {
        if(StringUtils.isEmpty(poolId) || CommonTools.isEmpty(settlementIdLst)){
            return Lists.newArrayList();
        }
        Condition condition = T_SALES_BILL.POOL_ID.eq(poolId).and(T_SALES_BILL.SETTLEMENT_ID.in(settlementIdLst));
        List<TSalesBillObj> salesBillObjList = create.select().from(T_SALES_BILL).where(condition).fetchInto(TSalesBillObj.class);
        return salesBillObjList;
    }

    /**

     * @param poolId
     * @param settlementIdLst
     * @return
     */
    private List<TSalesBillPoolPreviewItemObj> getItemObj(String poolId, List<String> settlementIdLst) {
        List<TSalesBillPoolPreviewItemObj> itemObjLst = Lists.newArrayList();
        if(StringUtils.isEmpty(poolId) || CommonTools.isEmpty(settlementIdLst)){
            return itemObjLst;
        }
        Condition condition = T_SALES_BILL_POOL_PREVIEW_ITEM.POOL_ID.eq(poolId).and(T_SALES_BILL_POOL_PREVIEW_ITEM.SETTLEMENT_ID.in(settlementIdLst));
        itemObjLst = create.select().from(T_SALES_BILL_POOL_PREVIEW_ITEM).where(condition).fetchInto(TSalesBillPoolPreviewItemObj.class);
        return itemObjLst;
    }

    /**
     * 保存规则：把组合规则、合并规则 应用到 当前的用户poolId、购销对上
     * @param poolId 用户结算池Id
     * @param sellerPurchaserHashKey  购销对key
     * @param salesBillConfigId           组合规则
     * @param userRole                用户角色
     * @return
     */
    
    @Transactional
    public Response modifySellerPurchaserRule(String poolId, String sellerPurchaserHashKey,
                                              String salesBillConfigId, Integer userRole) {
        checkUserRole(userRole);
        String errTitle = "为当前购销公司应用组合、合并规则失败:";
        if (CommonTools.isEmpty(salesBillConfigId)) {
            return Response.failed(errTitle + "配置规则ID不能为空!");
        }
        List<TSalesBillPoolPreviewObj> salesBillPoolPreviewLst = salesBillPollTools.getSalesBillPoolPreviewByPsKey(poolId, sellerPurchaserHashKey);
        if(CommonTools.isEmpty(salesBillPoolPreviewLst)) {
            return Response.failed(errTitle+"没找到你要配置规则的购销公司!");
        }

        TSalesBillObj salesBillObj = new TSalesBillObj();
        BeanUtils.copy(salesBillPoolPreviewLst.get(0), salesBillObj);
        salesBillObj.setUserId(contextService.currentUser());
//        List<TSalesBillMergeSplitRuleObj> compositRuleObjLst11 = salesBillConfigTools.getUserConfigRule(tSalesBillObj, RuleNameEnum.HBJS.getCode());

        ConfigList config = new ConfigList();
        config.setSalesBillConfigId(salesBillConfigId);
        Response<ConfigDetailList> ruleResponse = salesBillConfigService.salesBillConfigList(config, userRole, 0, 20, QueryFlagEnum.MR.getCode(), "");
        List<ConfigList> ruleList = Lists.newArrayList();
        if (ruleResponse.getCode().equals(Response.OK)) {
            ruleList = ruleResponse.getResult().getItemList();
        } else {
            return Response.failed(errTitle + "没找到对应的组合规则!");
        }

        //填充规则DTO
        TSalesBillMergeSplitRuleObj compositRuleObj = new TSalesBillMergeSplitRuleObj();
        TSalesBillMergeSplitRuleObj mergeRuleObj = new TSalesBillMergeSplitRuleObj();
        TSalesBillMergeSplitRuleObj beizhuRuleObj = new TSalesBillMergeSplitRuleObj();
        if (!CommonTools.isEmpty(ruleList)) {
            //组装组合和合并规则参数
//            List<SalesBillConfigDetail> zhRule = ruleList.get(0).getDetailList().stream().filter(a -> a.getName().equals(RuleNameEnum.JSZH.getCode())).collect(Collectors.toList());
//            List<SalesBillConfigDetail> hbRule = ruleList.get(0).getDetailList().stream().filter(a -> a.getName().equals(RuleNameEnum.HBJS.getCode())).collect(Collectors.toList());
//            List<SalesBillConfigDetail> jeRule = ruleList.get(0).getDetailList().stream().filter(a -> a.getName().equals(RuleNameEnum.JEHB.getCode())).collect(Collectors.toList());
//            List<SalesBillConfigDetail> zfRule = ruleList.get(0).getDetailList().stream().filter(a -> a.getName().equals(RuleNameEnum.DISCOUNT.getCode())).collect(Collectors.toList());
//            if (CommonTools.isEmpty(zhRule) || CommonTools.isEmpty(zhRule.get(0).getValue())
//                    || CommonTools.isEmpty(hbRule) || CommonTools.isEmpty(hbRule.get(0).getValue())
//                    || CommonTools.isEmpty(jeRule) || CommonTools.isEmpty(jeRule.get(0).getValue())
//                    ) {
//                return Response.failed(errTitle + "组合、合并或金额合并规则为空!");
//            }
            fulfillSalesBillSplitRuleObj(ruleList.get(0), compositRuleObj, poolId, sellerPurchaserHashKey, RuleNameEnum.JSZH.getCode());
            fulfillSalesBillSplitRuleObj(ruleList.get(0), mergeRuleObj, poolId, sellerPurchaserHashKey, RuleNameEnum.HBJS.getCode());
            fulfillSalesBillSplitRuleObj(ruleList.get(0), beizhuRuleObj, poolId, sellerPurchaserHashKey, RuleNameEnum.BZPJ.getCode());

        }

        boolean clearResult = salesBillTools.clearLocalSalesBillRule(poolId, sellerPurchaserHashKey);
        if (!clearResult) {
            return Response.failed(errTitle + "规则Id数据有误!");
        }

      /*  List<String> settlementIdLst = salesBillPoolPreviewLst.stream().map(preview -> {
            return preview.getSettlementId();
        }).collect(Collectors.toList());*/
        //List<String> idList = salesBillPoolPreviewLst.stream().map(TSalesBillPoolPreviewObj::getSettlementId).collect(Collectors.toList());
        //List<String> salesBillIdLst = getPreRemoveSalesBillObjLst(poolId, settlementIdLst);
        try {
            //移除当前结算池中的数据（ 这里的逻辑需要等待计算完毕，否则新移入结算池的数据采用新合并规则，老数据使用老规则，会出一个结算单明细采用了多个规则情况）
            //Response removeResponse = this.removeSalesBillFromPool(poolId, salesBillIdLst, userRole);  //移除一个购销对下的所有业务单，交给job执行
            //if (Response.OK.equals(removeResponse.getCode())) {
            //logger.info("移除成功,等待加入结算池");
            //另开一个线程处理
                /*executorService.execute(new Runnable() {
                    public void run() {*/
            //移除计算完毕后，才能插入新的规则。 这里的移除job执行可能运用了下面下入的规则而导致逻辑错！！
                        /*try {
                           Thread.sleep(80000);//暂停80s
                            salesBillTools.batchInsertSalesBillRule(compositRuleObj, mergeRuleObj);
                            //移除缓存中的规则
                            GetUserConfigRuleReq req0 = new GetUserConfigRuleReq();
                            BeanUtils.copy(salesBillObj, req0);
                            req0.setName(RuleNameEnum.HBJS.getCode());
                            GetUserConfigRuleReq req1 = new GetUserConfigRuleReq();
                            BeanUtils.copy(salesBillObj, req1);
                            req1.setName(RuleNameEnum.JSZH.getCode());
                            List keys = Lists.newArrayList(req0, req1);
                            salesBillConfigTools.clearCacheByKey(keys);
                            logger.info("modifySellerPurchaserRule重新加入结算池开始,poolId:{};salesBillIdLst:{};userRole:{};", poolId, salesBillIdLst, userRole);
                           // salesBillJoinPool(poolId, salesBillIdLst, userRole);//加入结算池
                            logger.info("modifySellerPurchaserRule重新加入结算池完成");
                        } catch (Exception e) {
                            logger.error("modifySellerPurchaserRule重新加入结算池exception :", e);
                        }*/
            //salesBillResetOutAndInPool(poolId, salesBillPoolPreviewLst, userRole, sellerPurchaserHashKey);
            salesBillResetOutAndInPool(poolId, salesBillPoolPreviewLst, userRole, salesBillObj, compositRuleObj, mergeRuleObj, beizhuRuleObj);
            // }
            // });
            //});
            //
             /*else {
                logger.info("modifySellerPurchaserRule移除业务单失败");
                return removeResponse;
            }*/
        } catch (SalesbillHandleException se) {
            logger.error("",se);
            logger.info(se.getMessage());
        } catch (Exception e) {
            logger.error("",e);
            return Response.failed(errTitle + "modifySellerPurchaserRule 为购销对key：" + sellerPurchaserHashKey + " 保存组合、合并规则失败！");
        }
        return Response.ok("保存组合、合并规则成功！");
    }

    @Transactional
    public void salesBillResetOutAndInPool(String poolId, List<TSalesBillPoolPreviewObj> salesBillPoolPreviewLst, Integer userRole, TSalesBillObj salesBillObj,
                                           TSalesBillMergeSplitRuleObj compositRuleObj, TSalesBillMergeSplitRuleObj mergeRuleObj, TSalesBillMergeSplitRuleObj beizhuRuleObj) throws Exception {
        logger.info("get pool lock {}",poolId);
        RedisLock redisLock = null;
        redisLock = applicationContext.getBean(RedisLock.class, "lock:" + poolId);
        boolean lock = redisLock.lock(6,1000 * 60 * 10);
        logger.info("get pool lock result {}",lock);
        if (!lock){
            logger.info("salesBillResetOutAndInPool get lock fail:{};" ,poolId);
            return;
        }
        try{
            List<String> settlementIdLst = salesBillPoolPreviewLst.stream().map(preview -> {
                return preview.getSettlementId();
            }).collect(Collectors.toList());
            List<TSalesBillObj> salesBillLst = getPreRemoveSalesBillObjLst(poolId, settlementIdLst);
            List<TSalesBillPoolPreviewItemObj>  itemObjLst = getItemObj(poolId, settlementIdLst);
            salesBillPoolTools.delSalesBillPoolPreviewBatch(salesBillPoolPreviewLst);
            salesBillPoolTools.delSalesBillPoolPreviewItemBatch(itemObjLst);

            salesBillTools.batchInsertSalesBillRule(compositRuleObj, mergeRuleObj, beizhuRuleObj);
            //移除缓存中的规则
            clearUserConfigRule(salesBillObj);

//            logger.info("modifySellerPurchaserRule重新加入结算池开始,poolId:{};salesBillLst:{};userRole:{};", poolId, salesBillLst, userRole);
            List<TSalesBillObj> newTSalesBillObjs = Lists.newArrayList();
            for (int i = 0; i < salesBillLst.size(); i++) {
                TSalesBillObj salesBill = salesBillLst.get(i);
                salesBill.setPoolId(poolId);
                TSalesBillObj newSalesBillObj = new TSalesBillObj();
                BeanUtils.copy(salesBill,newSalesBillObj);
                newSalesBillObj.setSalesBillId(salesBill.getSalesBillId());
                newSalesBillObj.setUserId(contextService.currentUser());
                newSalesBillObj.setStatus(SalesBillStatus.NORMAL.value());
                newSalesBillObj.setUploadConfirmFlag(SalesBillCatalog.ALREADY_JOIN_POLL.value());
                newSalesBillObj.setReceiveConfirmFlag(SalesBillCatalog.ALREADY_JOIN_POLL.value());
                newSalesBillObj.setComputeStatus(SalesBillComputeStatus.PENDING_COMPUTE.value());
                newSalesBillObj.setSettlementItemId(CommonTools.emptyString());
                newSalesBillObj.setSettlementNo(CommonTools.emptyString());
                newSalesBillObj.setSettlementId(CommonTools.emptyString());
                newSalesBillObj.setAddPoolType(SalesBillAddPoolType.ADD_2_POOL.value());
                newTSalesBillObjs.add(newSalesBillObj);
                historyTools.recordHistoryAfterCommit(SalesBillAction.JOIN_POLL, salesBill, newSalesBillObj,"");
            }
            if(newTSalesBillObjs.size() <2){
                logger.info("");
            }
//            logger.info("newTSalesBillObjs info {}",newTSalesBillObjs);
            //更新业务单
            updateBatch(Tables.T_SALES_BILL,newTSalesBillObjs);
            //更新业务单记录状态后，将结算池状态更新为待计算状态
            salesBillPoolService.updatePoolStatusForCompute(poolId);
        }catch(Exception e) {
            logger.error("salesBillResetOutAndInPool err:{}", e.getMessage());
            throw new Exception("salesBillResetOutAndInPool err");
        }finally {
            if(lock) {
                redisLock.unlock();
            }
//            callAfterCommitAsync(() ->{
//                sendSalesBillPoolJobMsg();
//            });


        }
    }

    public void clearUserConfigRule(TSalesBillObj salesBillObj) {
        GetUserConfigRuleReq req0 = new GetUserConfigRuleReq();
        BeanUtils.copy(salesBillObj, req0);
        req0.setName(RuleNameEnum.HBJS.getCode());
        GetUserConfigRuleReq req1 = new GetUserConfigRuleReq();
        BeanUtils.copy(salesBillObj, req1);
        req1.setName(RuleNameEnum.JSZH.getCode());
        GetUserConfigRuleReq req2 = new GetUserConfigRuleReq();
        BeanUtils.copy(salesBillObj, req2);
        req2.setName(RuleNameEnum.DISCOUNT.getCode());
        List keys = Lists.newArrayList(req0, req1, req2);
        salesBillConfigTools.clearCacheByKey(keys);
    }

    /**
     * 获取结算池中指定购销对下设置的规则列表
     * @param poolId  结算池Id
     * @param sellerPurchaserHashKey 购销对key
     * @param userRole  用户角色
     * @return
     */
    public Response getSalesBillMergeSplitRule(String poolId, String sellerPurchaserHashKey,Integer userRole) {
        checkUserRole(userRole);
        if (StringUtils.isEmpty(poolId) || StringUtils.isEmpty(sellerPurchaserHashKey)){
            return Response.failed("结算池poolId:"+poolId+",购销对sellerPurchaserHashKey: "+sellerPurchaserHashKey);
        }
        List<SalesBillMergeSplitRule> mergeSplitRuleLst = Lists.newArrayList();
        Map<String, TSalesBillMergeSplitRuleObj> mergeRuleMap = salesBillTools.getLocalSalesBillRule(poolId,sellerPurchaserHashKey);
        if (!CommonTools.isEmpty(mergeRuleMap)) {
            mergeSplitRuleLst.addAll(mergeRuleMap.values().stream().map(item->{
                SalesBillMergeSplitRule mergeSplitRule = new SalesBillMergeSplitRule();
                CommonTools.copyProperties(item, mergeSplitRule);
                return mergeSplitRule;
            }).collect(Collectors.toList()));
        }
        return Response.from(Response.OK, "获取购销公司组合、合并规则成功!", mergeSplitRuleLst);
    }

    public Response singleModifySalesBill(SalesbillItem salesBillItem, Integer userRole) {
        Response response = Response.ok("修改业务单成功！");
        CommonTools.assertNotNull(salesBillItem);
        CommonTools.assertNotEmpty(salesBillItem.getSalesBillId(),"业务单ID不能为空！");
        List<Condition> conditions = Lists.newArrayList(Tables.T_SALES_BILL.SALES_BILL_ID.eq(salesBillItem.getSalesBillId()));
        TSalesBillObj originObj = create.select().from(Tables.T_SALES_BILL).where(conditions).fetchOneInto(TSalesBillObj.class);
        if (originObj == null){
            return Response.failed("业务单号:"+salesBillItem.getSalesBillNo()+"不存在");
        }
        TSalesBillObj older = new TSalesBillObj();
        BeanUtils.copy(originObj,older);
        //判断如果COOPERATE_FLAG为1协同，不可修改购销对信息
        if (CooperationFlag.COOPERATE.value().equals(originObj.getCooperateFlag())) {
            salesBillItem.setSellerTaxNo(null);
            salesBillItem.setSellerName(null);
            salesBillItem.setPurchaserTaxNo(null);
            salesBillItem.setPurchaserName(null);
        }
        CommonTools.copyPropertiesIgnoreNull(salesBillItem, originObj);
        TSalesBillInterfaceObj obj = new TSalesBillInterfaceObj();
        BeanUtils.copy(originObj,obj);
        try {
            CommonTools.zeroBigDecimalFields(obj, TSalesBillInterfaceObj.class);
        }catch (Exception e) {
            logger.error("InterfaceObj中所有BigDecimal字段为null的时候，设置为BigDecimal.ZERO 发生异常", e);
        }

        if (PriceMethodType.WITHOUT_TAX.value().equals(obj.getPriceMethod())) {
            if ((BigDecimal.ZERO.compareTo(obj.getQuantity()) == 0 || CommonTools.isEmpty(String.valueOf(obj.getQuantity())))
                    && (BigDecimal.ZERO.compareTo(obj.getUnitPrice()) == 0 || CommonTools.isEmpty(String.valueOf(obj.getUnitPrice())))
                    && (BigDecimal.ZERO.compareTo(obj.getUnitPriceWithTax()) == 0 || CommonTools.isEmpty(String.valueOf(obj.getUnitPriceWithTax())))) {
                BigDecimal unitPrice = SalesBillPriceTools.getUnitPriceByUnitPriceWithTax(obj.getTaxRate(), obj.getUnitPriceWithTax());
                obj.setUnitPrice(unitPrice);
                obj.setQuantity(SalesBillPriceTools.getQuantityByUnitPriceAndWithoutTaxAmount(unitPrice, obj.getAmountWithoutTax()));
            }
        } else {
            if ((BigDecimal.ZERO.compareTo(obj.getQuantity()) == 0 || CommonTools.isEmpty(String.valueOf(obj.getQuantity())))
                    && (BigDecimal.ZERO.compareTo(obj.getUnitPrice()) == 0 || CommonTools.isEmpty(String.valueOf(obj.getUnitPrice())))
                    && (BigDecimal.ZERO.compareTo(obj.getUnitPriceWithTax()) == 0 || CommonTools.isEmpty(String.valueOf(obj.getUnitPriceWithTax())))) {
                BigDecimal unitPrice = SalesBillPriceTools.getUnitPriceWithTaxByUnitPrice(obj.getTaxRate(), obj.getUnitPrice());
                obj.setUnitPrice(unitPrice);
                obj.setQuantity(SalesBillPriceTools.getQuantityByUnitPriceAndWithTaxAmount(unitPrice, obj.getAmountWithTax()));
            }
        }
        StringBuilder checkMsg = new StringBuilder();
        String errors = salesBillImportFileHandler.salesBillInterfaceValidate(obj, 0);
        if(!CommonTools.isEmpty(errors)){
            checkMsg.append(errors);
        }
        //校验业务单状态
        String role = salesBillTools.convertRole(userRole);
        String uploadConfirmFlag = originObj.getUploadConfirmFlag();
        String receiveConfirmFlag = originObj.getReceiveConfirmFlag();
        if (!SalesBillStatus.NORMAL.value().equals(originObj.getStatus())){
            checkMsg.append("业务单【"+originObj.getSalesBillNo()+"】已作废或者删除，不可修改");
        }
        if (role.equals(originObj.getBusinessBillType())) {//是该角色上传的
            if (!SalesBillCatalog.CONFIRMING.value().equals(uploadConfirmFlag) &&
                    !SalesBillCatalog.CHANGING.value().equals(uploadConfirmFlag) &&
                    !SalesBillCatalog.WAITINGFORHANDLE.value().equals(uploadConfirmFlag)){
                checkMsg.append("业务单【"+originObj.getSalesBillNo()+"】状态已变更，不可修改");
            }
        }else {
            if (!SalesBillCatalog.CONFIRMING.value().equals(receiveConfirmFlag) &&
                    !SalesBillCatalog.CHANGING.value().equals(receiveConfirmFlag) &&
                    !SalesBillCatalog.WAITINGFORHANDLE.value().equals(receiveConfirmFlag)){
                checkMsg.append("业务单【"+originObj.getSalesBillNo()+"】状态已变更，不可修改");
            }
        }
        if(!CommonTools.isEmpty(checkMsg.toString())) {
            response.setCode(-1);
            response.setMessage(checkMsg.toString());
            return response;
        } else {
            CommonTools.copyPropertiesIgnoreNull(obj, originObj);
            String itemShortName = getItemShortName(obj.getGoodsTaxNo());
            originObj.setItemShortName(itemShortName);
            cooperationUtils.handlAutoFlag(originObj,userRole,SalesBillAction.UPDATE);
        }
        return response;
    }

    public String getItemShortName(String goodsTaxNo) {
        if(org.springframework.util.StringUtils.isEmpty(goodsTaxNo)) return "";
        TaxCatalogItem taxCatalogItem = localCacheManager.getTaxCodeItem(goodsTaxNo);
        return taxCatalogItem != null && taxCatalogItem.getItemShortName()!= null ? taxCatalogItem.getItemShortName() : "";
    }

    private boolean validateSalesBillModify(TSalesBillInterfaceObj bean, SalesbillItem salesBillItem, SalesBillAction action, List<String> errors) {
        if (SalesBillAction.UPDATE == action && (!ValidatorTools.validateCooperate(CooperationFlag.fromValue(bean.getCooperateFlag()), salesBillItem, errors))){
            return false;
        }
        CommonTools.copyPropertiesIgnoreNull(salesBillItem, bean);
        if ((BigDecimal.ZERO.compareTo(bean.getQuantity()) == 0 || CommonTools.isEmpty(String.valueOf(bean.getQuantity())))
                && (BigDecimal.ZERO.compareTo(bean.getUnitPrice()) == 0 || CommonTools.isEmpty(String.valueOf(bean.getUnitPrice())))
                && (BigDecimal.ZERO.compareTo(bean.getUnitPriceWithTax()) == 0 || CommonTools.isEmpty(String.valueOf(bean.getUnitPriceWithTax())))) {
            if (PriceMethodType.WITHOUT_TAX.value().equals(bean.getPriceMethod())) {
                bean.setUnitPriceWithTax(new BigDecimal("1"));
                BigDecimal unitPrice = SalesBillPriceTools.getUnitPriceByUnitPriceWithTax(bean.getTaxRate(), bean.getUnitPriceWithTax());
                bean.setUnitPrice(unitPrice);
                bean.setQuantity(SalesBillPriceTools.getQuantityByUnitPriceAndWithoutTaxAmount(unitPrice, bean.getAmountWithoutTax()));
            } else if (PriceMethodType.WITH_TAX.value().equals(bean.getPriceMethod())) {
                bean.setUnitPrice(new BigDecimal("1"));
                BigDecimal unitPriceWithTax = SalesBillPriceTools.getUnitPriceWithTaxByUnitPrice(bean.getTaxRate(), bean.getUnitPrice());
                bean.setUnitPriceWithTax(unitPriceWithTax);
                bean.setQuantity(SalesBillPriceTools.getQuantityByUnitPriceAndWithTaxAmount(unitPriceWithTax, bean.getAmountWithTax()));
            }
        }
        String error = salesBillImportFileHandler.salesBillInterfaceValidate(bean,0);
        if (!CommonTools.isEmpty(error)) {
            errors.add(error);
            return false;
        }
        return true;
    }

    public List<String> getValidateSuccessSalesBillIds(List<String> salesBillIds, SalesbillItem salesBillItem, Integer userRole) {
        String role = salesBillTools.convertRole(userRole);
        Condition conditionUpload = T_SALES_BILL.BUSINESS_BILL_TYPE.eq(role).and(T_SALES_BILL.UPLOAD_CONFIRM_FLAG.in(SalesBillCatalog.CONFIRMING.value(), SalesBillCatalog.CHANGING.value(), SalesBillCatalog.WAITINGFORHANDLE.value()));
        Condition conditionReceive = T_SALES_BILL.BUSINESS_BILL_TYPE.ne(role).and(T_SALES_BILL.RECEIVE_CONFIRM_FLAG.in(SalesBillCatalog.CONFIRMING.value(), SalesBillCatalog.CHANGING.value(), SalesBillCatalog.WAITINGFORHANDLE.value()));
        List<TSalesBillInterfaceObj> beans = create.select().from(T_SALES_BILL)
                .where(T_SALES_BILL.SALES_BILL_ID.in(salesBillIds))
                .and(T_SALES_BILL.STATUS.eq(SalesBillStatus.NORMAL.value()))
                .and(conditionUpload.or(conditionReceive))
                .fetchInto(TSalesBillInterfaceObj.class);
        List<String> successSalesBillIds = new LinkedList<>();
        for (TSalesBillInterfaceObj bean : beans) {
            List<String> errors = new LinkedList<>();
            boolean isSuccess = validateSalesBillModify(bean, salesBillItem, SalesBillAction.UPDATE, errors);
            if (isSuccess) {
                successSalesBillIds.add(bean.getSalesBillId());
            }
        }
        return successSalesBillIds;
    }

    public Response salesBillBatchModifyValidate(List<String> salesBillIds, SalesbillItem salesBillItem) {
        List<TSalesBillInterfaceObj> beans = create.select().from(T_SALES_BILL).where(T_SALES_BILL.SALES_BILL_ID.in(salesBillIds)).fetchInto(TSalesBillInterfaceObj.class);
        List<String> successSalesBillIds = Lists.newLinkedList();
        List<String> errorSalesBillMessage = Lists.newLinkedList();
        for (TSalesBillInterfaceObj bean : beans) {
            List<String> errors  = new LinkedList<>();

            boolean isSuccess = validateSalesBillModify(bean, salesBillItem, SalesBillAction.UPDATE, errors);
            if (isSuccess) {
                successSalesBillIds.add(salesBillItem.getSalesBillId());
            } else {
                errorSalesBillMessage.add(errors.toString());
            }
        }
        SalesBillBatchModifyValidate result = new SalesBillBatchModifyValidate();
        result.setTotalCount(beans.size() + "");
        result.setSuccessCount(successSalesBillIds.size() + "");
        result.setFailCount(errorSalesBillMessage.size() + "");
        result.setSalesBillIds(successSalesBillIds);
        result.setErrList(errorSalesBillMessage);
        return Response.from(Response.OK, "本次操作业务单共" + beans.size() + "条,其中" + errorSalesBillMessage.size() + "条不符合加入条件，是否确认批量修改发票类型", result);
    }

    public StringBuilder validateSalesBillModify(SalesbillItem salesBillItem, String flag) {
        StringBuilder checkMsg = new StringBuilder();
        List<Condition> conditions = Lists.newArrayList(Tables.T_SALES_BILL.SALES_BILL_ID.eq(salesBillItem.getSalesBillId()));
        TSalesBillObj originObj =  create.select().from(Tables.T_SALES_BILL).where(conditions).fetchOneInto(TSalesBillObj.class);
        if(originObj == null){
            checkMsg.append("业务单号:"+salesBillItem.getSalesBillNo()+"不存在");
            return checkMsg;
        }

        if (SalesBillAction.UPDATE.value().equals(flag)){
            CooperationFlag flat = CooperationFlag.fromValue(originObj.getCooperateFlag());
            List<String> errors = new LinkedList<>();
            boolean success = ValidatorTools.validateCooperate(flat,salesBillItem, errors);
            if (!success) {
                checkMsg.append(errors.toString());
                return checkMsg;
            }
        }
        CommonTools.copyPropertiesIgnoreNull(salesBillItem, originObj);
        TSalesBillInterfaceObj obj = new TSalesBillInterfaceObj();
        BeanUtils.copy(originObj, obj);
        try {
            CommonTools.zeroBigDecimalFields(obj, TSalesBillInterfaceObj.class);
        } catch (Exception e) {
            logger.error("InterfaceObj中所有BigDecimal字段为null的时候，设置为BigDecimal.ZERO 发生异常", e);
        }

        if ((BigDecimal.ZERO.compareTo(obj.getQuantity()) == 0 || CommonTools.isEmpty(String.valueOf(obj.getQuantity())))
                && (BigDecimal.ZERO.compareTo(obj.getUnitPrice()) == 0 || CommonTools.isEmpty(String.valueOf(obj.getUnitPrice())))
                && (BigDecimal.ZERO.compareTo(obj.getUnitPriceWithTax()) == 0 || CommonTools.isEmpty(String.valueOf(obj.getUnitPriceWithTax())))) {
            if (PriceMethodType.WITHOUT_TAX.value().equals(obj.getPriceMethod())) {
                obj.setUnitPriceWithTax(new BigDecimal("1"));
                BigDecimal unitPrice = SalesBillPriceTools.getUnitPriceByUnitPriceWithTax(obj.getTaxRate(), obj.getUnitPriceWithTax());
                obj.setUnitPrice(unitPrice);
                obj.setQuantity(SalesBillPriceTools.getQuantityByUnitPriceAndWithoutTaxAmount(unitPrice, obj.getAmountWithoutTax()));
            } else if (PriceMethodType.WITH_TAX.value().equals(obj.getPriceMethod())) {
                obj.setUnitPrice(new BigDecimal("1"));
                BigDecimal unitPriceWithTax = SalesBillPriceTools.getUnitPriceWithTaxByUnitPrice(obj.getTaxRate(), obj.getUnitPrice());
                obj.setUnitPriceWithTax(unitPriceWithTax);
                obj.setQuantity(SalesBillPriceTools.getQuantityByUnitPriceAndWithTaxAmount(unitPriceWithTax, obj.getAmountWithTax()));
            }
        }

        String errors = salesBillImportFileHandler.salesBillInterfaceValidate(obj, 0);
        if(!CommonTools.isEmpty(errors)){
            checkMsg.append(errors);
        }
        return checkMsg;
    }

    @MultipartDistributedLock
    public void batchModifySalesBill(List<String> salesBillIds, SalesbillItem salesBillItem, Integer userRole) {
        List<TSalesBillObj> beans = create.select().from(T_SALES_BILL).where(T_SALES_BILL.SALES_BILL_ID.in(salesBillIds)).fetchInto(TSalesBillObj.class);
        //判断如果COOPERATE_FLAG为1协同，不可修改购销对信息
        for(TSalesBillObj bean : beans) {
            String sellerTaxNo = bean.getSellerTaxNo();
            String sellerName = bean.getSellerName();
            String purchaserTaxNo = bean.getPurchaserTaxNo();
            String purchaserName = bean.getPurchaserName();
            CommonTools.copyPropertiesIgnoreNull(salesBillItem, bean);
            if (CooperationFlag.COOPERATE.value().equals(bean.getCooperateFlag())) {
                bean.setSellerTaxNo(sellerTaxNo);
                bean.setSellerName(sellerName);
                bean.setPurchaserTaxNo(purchaserTaxNo);
                bean.setPurchaserName(purchaserName);
            }
            String itemShortName = getItemShortName(bean.getGoodsTaxNo());
            bean.setItemShortName(itemShortName);
        }
        cooperationUtils.batchHandlAutoFlag(beans, userRole, SalesBillAction.UPDATE);
    }

    public Response getSalesBillLastMergeAndSplitHistory(String salesBillId, Integer userRole) {
        if(StringUtils.isBlank(salesBillId)) {
            Response.failed("当前业务单号不能为空");
        }
        return salesBillHisService.getSalesBillLastMergeAndSplitHistory(salesBillId);
    }

    /**
     * 确认业务单
     * @param reqList
     * @param userRole
     * @return
     */
    public Response confirmSalesBillByConditions(ConditionReqList reqList, Integer userRole) {
        CommonTools.assertNotNull(reqList);
        checkUserRole(userRole);

        List<String> data = salesBillWithEsService.getSalesBillIds(reqList, reqList.getCatalog(), userRole, LIMIT_COUNT);
        if (CommonTools.isEmpty(data)){
            return Response.from(Response.Fail,"未找到满足条件的业务单");
        }
        Response response = salesBillTools.confirmSalesBillByIds(data, userRole);

        return response;
    }


    /**
     * 确认前的校验
     * @param ids
     * @param userRole
     * @return
     */
    public Response preConfirmSalesBill(List<String> ids, Integer userRole) {
        logger.info("===============================进入preConfirmSalesBill方法=============================");
        CommonTools.assertNotNull(ids);
        checkUserRole(userRole);

        if (ids.size() > 100){
            return Response.from(Response.Fail,"一次确认业务单的数量不能超过100条");
        }
        Response response = salesBillTools.preConfirmSalesBill(ids, userRole);
        Map map = (Map) response.getResult();
        return Response.from(Response.OK,"校验完成",map);
    }

    public Response confirmSalesBillByCondition(List<SalesBillConditionRequest> conditions, String catalog,Integer userRole) {
        Condition condition=salesBillTools.getConditionByConditions(conditions, catalog, userRole).and(salesBillPermissionTools.getSalesBillPermissionCondition(userRole));
        List<String> salesBillIds = create.select(Tables.T_SALES_BILL.SALES_BILL_ID).from(T_SALES_BILL).where(condition).fetchInto(String.class);
        return this.confirmSalesBillByIds(salesBillIds, userRole);
    }

    /**
     * 确认业务单
     * @param ids
     * @param userRole
     * @return
     */
    public Response confirmSalesBillByIds(List<String> ids, Integer userRole) {
        Response response = salesBillTools.confirmSalesBillByIds(ids, userRole);
        return Response.from(Response.OK,"确认成功",response.getResult());
    }

    public Response reconfirmSalesBill(List<String> id, Integer userRole) {
        logger.info("===============================进入reconfirmSalesBill方法=============================");
        Response response = preReconfirmSalesBill(id, userRole);
        if (Response.Fail.equals(response.getCode())){
            return response;
        }
        Map result = (Map) response.getResult();
        List<TSalesBillObj> salesBillObjs = (List<TSalesBillObj>) result.get("successSalesBill");
        if (CommonTools.isEmpty(salesBillObjs)){
            return Response.from(Response.Fail,"未找到可重新确认的业务单");
        }
        for (int i=0;i<salesBillObjs.size();i++){
            TSalesBillObj salesBillObj = salesBillObjs.get(i);
            response = cooperationUtils.handlAutoFlag(salesBillObj, userRole, SalesBillAction.RECONFIRM);
        }

        return Response.from(Response.OK,"重新确认成功");
    }

    public Response preReconfirmSalesBill(List<String> id, Integer userRole) {
        logger.info("===============================进入preReconfirmSalesBill方法=============================");
        Response response = salesBillTools.preReconfirmSalesBill(id, userRole);

        return response;
    }

    /**
     * 作废前的校验
     *
     * @param reqList
     * @param userRole
     * @return
     */
    public Response preAbandonSalesBillByIds(ConditionReqList reqList, Integer userRole) {
        logger.info("=====================================作废前的校验=====================================");
        CommonTools.assertNotNull(reqList);
        checkUserRole(userRole);

        //获取catalog状态栏标记
        String catalog = reqList.getCatalog();

        //获取salesBill Id
        List<String> data = salesBillWithEsService.getSalesBillIds(reqList, catalog, userRole, LIMIT_COUNT);

        if (CommonTools.isEmpty(data)) {
            return Response.failed("要作废的业务单Id集合不能为空!");
        }
        List conditions = Lists.newArrayList();
        conditions.add(Tables.T_SALES_BILL.SALES_BILL_ID.in(data));
        conditions.add(Tables.T_SALES_BILL.STATUS.eq(SalesBillStatus.NORMAL.value()));
        List<TSalesBillObj> salesBillObjList = dao.queryObj(Tables.T_SALES_BILL,conditions);
        if (salesBillObjList.size()==0){
            return Response.failed("作废失败，根据参数未查询到业务单数据");
        }
        int success = 0;
        String role = salesBillTools.convertRole(userRole);
        List<TSalesBillObj> successSalesBillObj = Lists.newArrayList();
        for (int i=0;i<salesBillObjList.size();i++){
            TSalesBillObj salesBillObj = salesBillObjList.get(i);
            String uploadConfirmFlag = salesBillObj.getUploadConfirmFlag();
            if (!role.equals(salesBillObj.getBusinessBillType())){
                continue;
            }
            if(!SalesBillCatalog.WAITINGFORHANDLE.value().equals(uploadConfirmFlag)
                    && !SalesBillCatalog.CONFIRMING.value().equals(uploadConfirmFlag)
                    && !SalesBillCatalog.CHANGING.value().equals(uploadConfirmFlag)){
                continue;
            }
            success++;
            successSalesBillObj.add(salesBillObj);
        }

        Map map = Maps.newHashMap();
        map.put("success",success);
        map.put("fail",data.size() - success);
        map.put("successSalesBillObj",successSalesBillObj);
        return Response.from(Response.OK,"",map);
    }

    /**
     * 取消作废前的校验
     *
     * @param reqList
     * @param userRole
     * @return
     */
    public Response preCancelAbandonSalesBillByIds(ConditionReqList reqList, Integer userRole) {

        //获取catalog状态栏标记
        String catalog = reqList.getCatalog();
        //获取SalesBill Id
        List<String> data = salesBillWithEsService.getSalesBillIds(reqList,catalog,userRole, LIMIT_COUNT);

        List conditions = Lists.newArrayList();
        conditions.add(Tables.T_SALES_BILL.SALES_BILL_ID.in(data));
        conditions.add(Tables.T_SALES_BILL.STATUS.eq(SalesBillStatus.INVALID.value()));
        List<TSalesBillObj> salesBillObjList = dao.queryObj(Tables.T_SALES_BILL,conditions);
        if (CommonTools.isEmpty(salesBillObjList)){
            return Response.from(Response.Fail,"作废失败，未查询到数据");
        }
        int success = 0;
        String role = salesBillTools.convertRole(userRole);
        List<TSalesBillObj> salesBillObjs = Lists.newArrayList();
        for (int i=0;i<salesBillObjList.size();i++){
            TSalesBillObj salesBillObj = salesBillObjList.get(i);
            String businessBillType = salesBillObj.getBusinessBillType();
            if (!role.equals(businessBillType)){
                continue;
            }
            success++;
            salesBillObjs.add(salesBillObj);
        }
        Map map = Maps.newHashMap();
        map.put("success",success);
        map.put("fail",data.size() - success);
        map.put("successSalesBillObj",salesBillObjs);
        return Response.from(Response.OK,"",map);
    }

    public Map<String, Object> getSplitRollbackList(String salesBillId, Integer userRole) {
        String parentSalesBillId = create.select(T_SALES_BILL.PARENT_SALES_BILL_ID).from(T_SALES_BILL).where(T_SALES_BILL.SALES_BILL_ID.eq(salesBillId)).fetchOne().value1();
        Map<String, Object> result = new HashMap<>();
        List<SalesbillItem> before = create.select().from(T_SALES_BILL).where(T_SALES_BILL.SALES_BILL_ID.eq(parentSalesBillId)).fetchInto(SalesbillItem.class);
        result.put("beforeSalesbillItems", before);
        List<SalesbillItem> after = create.select().from(T_SALES_BILL).where(T_SALES_BILL.PARENT_SALES_BILL_ID.eq(parentSalesBillId)).fetchInto(SalesbillItem.class);
        result.put("afterSalesbillItems", after);
        return result;
    }

    public Map<String, Object> getMergeRollbackList(String salesBillId, Integer userRole) {
        Map<String, Object> result = new HashMap<>();
        List<SalesbillItem> after = create.select().from(T_SALES_BILL).where(T_SALES_BILL.SALES_BILL_ID.eq(salesBillId)).fetchInto(SalesbillItem.class);
        result.put("afterSalesbillItems", after);
        List<SalesbillItem> before = create.select().from(T_SALES_BILL).where(T_SALES_BILL.PARENT_SALES_BILL_ID.eq(salesBillId)).fetchInto(SalesbillItem.class);
        result.put("beforeSalesbillItems", before);
        return result;
    }
}