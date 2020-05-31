package com.process.txnbatch.processor;

import com.common.txnintegration.model.TxnModel;
import com.common.txnintegration.req.TxnReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.text.SimpleDateFormat;

public class TxnItemProcessor implements ItemProcessor<TxnModel, TxnReq> {

    private static final Logger log = LoggerFactory.getLogger(TxnItemProcessor.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public TxnReq process(TxnModel txnModel) throws Exception {
        final String accountNumber = txnModel.getAccountNumber();
        final String txnAmount = txnModel.getTxnAmount();
        final String description = txnModel.getDescription();
        final String txnDate = txnModel.getTxnDate();
        final String txnTime = txnModel.getTxnTime();
        final Long customerId = Long.parseLong(txnModel.getCustomerId());
        log.info("Converting txnModel");

        final TxnReq req = new TxnReq();
        req.setAccountNumber(accountNumber);
        req.setTxnAmount(txnAmount);
        req.setDescription(description);
        req.setTxnDate(txnDate);
        req.setTxnTime(txnTime);
        req.setCustomerId(customerId);
        log.info("Converted (" + txnModel + ") into (" + req);

        return req;
    }
}
