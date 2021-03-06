package javaeetutorial.websimplemessage;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;
import javax.jms.Queue;

@Named
@RequestScoped
public class SenderBean {

    static final Logger logger = Logger.getLogger("SenderBean");
    @Inject
    private JMSContext context;
    @Resource(mappedName = "java:jboss/javaeetutorial/jms/queue/test") //lookup = "jms/Queue",
    private Queue queue;
    private String messageText;

    /**
     * Creates a new instance of SenderBean
     */
    public SenderBean() {
    }

    /**
     * Get the value of messageText
     *
     * @return the value of messageText
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * Set the value of messageText
     *
     * @param messageText new value of messageText
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    /*
     * Within a try-with-resources block, create context.
     * Create message.
     * Create producer and send message.
     * Create FacesMessage to display on page.
     */
    public String sendMessage() {
        try {
            String text = "Message from producer: " + messageText;
            context.createProducer().send(queue, text);

            FacesMessage facesMessage =
                    new FacesMessage("Sent message: " + text);
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        } catch (JMSRuntimeException t) {
            logger.log(Level.SEVERE,
                    "SenderBean.sendMessage: Exception: {0}",
                    t.toString());
        }
        
        return "";
    }
}
