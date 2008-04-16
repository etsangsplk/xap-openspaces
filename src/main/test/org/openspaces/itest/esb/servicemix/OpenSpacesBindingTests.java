package org.openspaces.itest.esb.servicemix;

import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.openspaces.core.GigaSpace;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * Testing <code>org.openspaces.esb.servicemix.OpenSpacesInBinding</code> and
 * <code>org.openspaces.esb.servicemix.OpenSpacesOutBinding</code>
 */
public class OpenSpacesBindingTests extends SpringTestSupport {

    protected Receiver receiver;

    private GigaSpace gigaSpace;

    protected void setUp() throws Exception {
        super.setUp();
        gigaSpace = (GigaSpace) getBean("gigaSpace");
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/openspaces/itest/esb/servicemix/binding.xml");
    }

    public void test() {
        int numOfMsgs = 10;
        for (int i = 0; i < numOfMsgs; i++) {
            Message msg = new Message("hello " + i, false);
            gigaSpace.write(msg);
        }
        for (int i = 0; i < numOfMsgs; i++) {
            Message msg = new Message("hello " + i, true);
            Message message = gigaSpace.take(msg, 5000);
            assertNotNull(message);
        }
    }

}
