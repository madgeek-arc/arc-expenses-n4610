package arc.expenses.n4160.service;


import arc.athenarc.n4160.domain.Project;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("mailService")
public class MailService {

    private static Logger logger = LogManager.getLogger(MailService.class);

    @Autowired
    @Qualifier("jmsQueueTemplate")
    JmsTemplate jmsTemplate;

    @Value("${request.approval.url}")
    private String requestApprovalUrl;

    @Value("${request.payment.url}")
    private String requestPaymentUrl;

    @Value("${request.budget.url}")
    private String requestBudgetUrl;

    @Autowired
    private ProjectServiceImpl projectService;

    public void sendMail(String type, String requestId, String projectAcronym, String creationDate, String finalAmount, String subject, boolean isPayment, String subId, List<String> whoTo){

        logger.info("Sending mail of type "+type + " to " + whoTo.stream().collect(Collectors.joining(",")));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message_type",type);
        jsonObject.put("request_id",requestId);
        jsonObject.put("project_acronym",projectAcronym);
        jsonObject.put("creation_date",new SimpleDateFormat("dd-MM-yyyy").format(new Date(Long.parseLong(creationDate))));
        jsonObject.put("final_amount",finalAmount);
        jsonObject.put("subject",subject);
        if(isPayment)
            jsonObject.put("url",requestPaymentUrl+subId);
        else
            jsonObject.put("url",requestApprovalUrl+subId);

        jsonObject.put("to",whoTo);

        logger.info(jsonObject.toString());
        jmsTemplate.convertAndSend("mailbox", jsonObject.toString());
    }

    public void sendMail(String type, String budgetId, String creationDate, String projectId, List<String> sendTo){
        type = type.concat("_BUDGET");
        logger.info("Sending mail of type "+type + " to " + sendTo.stream().collect(Collectors.joining(",")));

        Project project = projectService.get(projectId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message_type",type);
        jsonObject.put("budget_id",budgetId);
        jsonObject.put("project_acronym",project.getAcronym());
        jsonObject.put("creation_date",new SimpleDateFormat("dd-MM-yyyy").format(new Date(Long.parseLong(creationDate))));
        jsonObject.put("url",requestBudgetUrl+budgetId);

        jsonObject.put("to",sendTo);

        logger.info(jsonObject.toString());

        jmsTemplate.convertAndSend("mailbox", jsonObject.toString());

    }

}
