/*******************************************************************************
 *
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.openspaces.utest.core.map.simple;

import com.j_spaces.map.IMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaMap;
import org.openspaces.core.SpaceTimeoutException;
import org.openspaces.core.map.LockHandle;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;


/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractMapTests {

    @Resource
    protected IMap map;

    @Resource
    protected GigaMap gigaMap;

    protected AbstractMapTests() {

    }

    @Before
    public void onSetUp() throws Exception {
        gigaMap.clear(true);
    }

    public void testSimpleMapOperations() {
        map.put("1", "value");

        assertEquals("value", map.get("1"));

        assertEquals("value", map.remove("1"));

    }

    @Ignore("Requires further investigation")
    public void testSimpleGigaMapOperations() {
        gigaMap.put("1", "value");
        assertEquals("value", gigaMap.get("1"));
        assertEquals("value", gigaMap.remove("1"));
    }

    @Test
    public void testSimpleLock() {
        gigaMap.put("2", "value");
        gigaMap.lock("2");
        assertTrue(gigaMap.isLocked("2"));
        try {
            gigaMap.put("2", "value1");
            fail();
        } catch (SpaceTimeoutException e) {
            // all is well, we are locked
        }
        gigaMap.unlock("2");
        gigaMap.put("2", "value2");
    }

    @Test
    public void testLockOnNonExistingValue() {
        gigaMap.lock("2");
        assertTrue(gigaMap.isLocked("2"));
        try {
            gigaMap.put("2", "value1");
            fail();
        } catch (SpaceTimeoutException e) {
            // all is well, we are locked
        }
        gigaMap.unlock("2");
        gigaMap.put("2", "value2");
    }

    @Test
    public void testSimpleLockWithLockHandle() {
        gigaMap.put("1", "value");
        LockHandle lockHandle = gigaMap.lock("1");
        gigaMap.put("1", "value1", lockHandle);
        try {
            gigaMap.put("1", "value2");
            fail();
        } catch (SpaceTimeoutException e) {
            // all is well, we are locked
        }
        gigaMap.unlock("1");
        assertEquals("value1", gigaMap.get("1"));
        gigaMap.put("1", "value3");
    }

    @Test
    public void testMultiThreadedLockAndUnlock() {
        gigaMap.put("1", "value");
        gigaMap.lock("1");
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    gigaMap.lock("1");
                } catch (Exception e) {
                    fail();
                }
            }
        });
        thread.start();
        gigaMap.unlock("1");
    }


}


