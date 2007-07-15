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

package org.openspaces.core.properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * A Spring {@link BeanPostProcessor} that process beans that implement
 * {@link BeanLevelPropertiesAware} or {@link BeanLevelMergedPropertiesAware} based on the provided
 * {@link BeanLevelProperties}.
 * 
 * @author kimchy
 * @see BeanLevelProperties
 * @see BeanLevelMergedPropertiesAware
 * @see BeanLevelPropertiesAware
 */
public class BeanLevelPropertyBeanPostProcessor implements BeanPostProcessor {

    private BeanLevelProperties beanLevelProperties;

    /**
     * Constructs a new bean level bean post processor based on the provided bean level properties.
     * 
     * @param beanLevelProperties
     *            The bean level properties to be used for injection
     */
    public BeanLevelPropertyBeanPostProcessor(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    /**
     * Post process a given bean. If the bean implements {@link BeanLevelPropertiesAware} the
     * provided {@link BeanLevelProperties} will be injected to it. If the bean implements
     * {@link BeanLevelMergedPropertiesAware} then the merged properties based on the provided
     * beanName will be injected (using {@link BeanLevelProperties#getMergedBeanProperties(String)}).
     * 
     * @param bean
     *            The bean to possibly perform injection of {@link BeanLevelProperties}
     * @param beanName
     *            The bean name
     * @return The bean unmodified
     * @throws BeansException
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof BeanLevelMergedPropertiesAware) {
            ((BeanLevelMergedPropertiesAware) bean).setMergedBeanLevelProperties(beanLevelProperties.getMergedBeanProperties(beanName));
        }
        if (bean instanceof BeanLevelPropertiesAware) {
            ((BeanLevelPropertiesAware) bean).setBeanLevelProperties(beanLevelProperties);
        }
        return bean;
    }

    /**
     * A no op, just returned the bean itself.
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
