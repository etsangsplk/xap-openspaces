/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.itest.core.space.mode.annotations;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @author shaiw
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/core/space/mode/annotations/mode-annotations.xml")
public class SpaceModeAnnotationsTest   { 

    private static final int TIMEOUT = 10000;
    
     @Autowired protected SpaceModeBean modeListener;

     @Autowired protected GigaSpace gigaSpace1;

     @Autowired protected GigaSpace gigaSpace2;

    public SpaceModeAnnotationsTest() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/mode/annotations/mode-annotations.xml"};
    }

     @Test public void testSpaceModeAnnotations() {
        assertNotNull(modeListener.state);
        assertNotNull(gigaSpace1);
        assertNotNull(gigaSpace2);
        assertEquals(2, modeListener.state.size());
        assertTrue(modeListener.state.containsKey(gigaSpace1.getSpace().getURL().toString()));
        assertTrue(modeListener.state.containsKey(gigaSpace2.getSpace().getURL().toString()));
        assertEquals(SpaceMode.PRIMARY, modeListener.state.get(gigaSpace1.getSpace().getURL().toString()));
        assertEquals(SpaceMode.BACKUP, modeListener.state.get(gigaSpace2.getSpace().getURL().toString()));

        // shut down space1 and wait for active election to do its stuff
        try {
            gigaSpace1.getSpace().getDirectProxy().shutdown();
            Thread.sleep(TIMEOUT);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
        // check the space2 is now the primary
        assertEquals(SpaceMode.PRIMARY, modeListener.state.get(gigaSpace2.getSpace().getURL().toString()));
    }
}

