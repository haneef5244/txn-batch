package com.process.txnbatch.writer;

import com.common.txnintegration.req.TxnReq;
import com.common.txnintegration.util.ResourceConstantUtil;
import com.process.txnbatch.util.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class TxnItemWriter implements ItemWriter<TxnReq>, Closeable {

    private static final Logger log = LoggerFactory.getLogger(TxnItemWriter.class);

    @Autowired
    AuthService authService;

    @Override
    public void close() throws IOException {

    }

    @Override
    public void write(List<? extends TxnReq> list) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        log.info("Results contains {}",list.size()," counts");
        for (TxnReq txn : list) {

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/graphql");
            headers.add("Authorization", "Bearer " + authService.getTokenInstance());
            String query1 =
                    String.format("mutation {\n" +
                                    "  createTxn(accountNumber:\"%s\", txnAmount:\"%s\", description:\"%s\", txnDate: \"%s\", txnTime:\"%s\", customerId:%d) {\n" +
                                    "    accountNumber\n" +
                                    "    txnDate\n" +
                                    "  }\n" +
                                    "}\n"
                            ,
                            txn.getAccountNumber(),
                            txn.getTxnAmount(),
                            txn.getDescription(),
                            txn.getTxnDate(),
                            txn.getTxnTime(),
                            txn.getCustomerId());

            ResponseEntity<String> response = restTemplate.postForEntity(ResourceConstantUtil.TXN_CUSTOMER_ENDPOINT, new HttpEntity<>(query1, headers), String.class);
            log.info("Response={}",response);
        }
    }

}
