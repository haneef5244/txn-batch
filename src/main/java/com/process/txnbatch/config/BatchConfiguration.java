package com.process.txnbatch.config;

import com.common.txnintegration.model.TxnModel;
import com.common.txnintegration.req.TxnReq;
import com.process.txnbatch.processor.TxnItemProcessor;
import com.process.txnbatch.util.AuthService;
import com.process.txnbatch.writer.TxnItemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfiguration {

    Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private AuthService authService;

    @Autowired
    JobLauncher jobLauncher;

    @Bean
    public FlatFileItemReader<TxnModel> reader() {
        return new FlatFileItemReaderBuilder<TxnModel>()
                .name("personItemReader")
                .resource(new ClassPathResource("dataSource.txt"))
                .linesToSkip(1)
                .delimited()
                .delimiter("|")
                .names(new String[]{"ACCOUNT_NUMBER", "TRX_AMOUNT", "DESCRIPTION",
                "TRX_DATE", "TRX_TIME", "CUSTOMER_ID"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<TxnModel>() {{
                    setTargetType(TxnModel.class);
                }})
                .build();
    }

    @Bean
    public TxnItemProcessor processor() {
        return new TxnItemProcessor();
    }

    @Bean
    public TxnItemWriter writer() {
        return new TxnItemWriter();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<TxnModel, TxnReq> chunk(100)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job txnJob() {
        return jobBuilderFactory.get("txnJob")
                .start(step1())
                .build();
    }

    @Scheduled(cron = "${spring.batch.schedule.process.txn}")
    public void run() throws Exception{
        authService.getNewToken();
        jobLauncher.run(
                txnJob(),
                new JobParametersBuilder().addLong("uniqueness",
                        System.nanoTime()).toJobParameters()
        );
    }

}
