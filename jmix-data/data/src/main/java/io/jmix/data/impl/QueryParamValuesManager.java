/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.data.impl;

import io.jmix.data.QueryParamValueProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Encapsulates invocations of registered {@link QueryParamValueProvider}s.
 */
@Component("data_QueryParamValuesManager")
public class QueryParamValuesManager {

    @Autowired
    private List<QueryParamValueProvider> queryParamValueProviders;

    public boolean supports(String paramName) {
        for (QueryParamValueProvider provider : queryParamValueProviders) {
            if (provider.supports(paramName)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public Object getValue(String paramName) {
        for (QueryParamValueProvider provider : queryParamValueProviders) {
            if (provider.supports(paramName)) {
                return provider.getValue(paramName);
            }
        }
        return null;
    }

}
