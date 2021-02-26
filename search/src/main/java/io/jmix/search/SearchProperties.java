/*
 * Copyright 2020 Haulmont.
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

package io.jmix.search;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.search")
@ConstructorBinding
public class SearchProperties {

    protected final int esSearchSize;
    protected final int maxSearchPageCount;
    protected final int searchReloadEntitiesBatchSize;
    protected final int processQueueBatchSize;
    protected final int maxProcessedQueueItemsPerExecution;
    protected final int reindexEntityEnqueueBatchSize;

    protected final boolean autoMapIndexFileContent;

    public SearchProperties(
            @DefaultValue("100") int esSearchSize,
            @DefaultValue("100") int maxSearchPageCount,
            @DefaultValue("100") int searchReloadEntitiesBatchSize,
            @DefaultValue("100") int processQueueBatchSize,
            @DefaultValue("1000") int maxProcessedQueueItemsPerExecution,
            @DefaultValue("100") int reindexEntityEnqueueBatchSize,
            @DefaultValue("false") boolean autoMapIndexFileContent) {
        this.esSearchSize = esSearchSize;
        this.maxSearchPageCount = maxSearchPageCount;
        this.searchReloadEntitiesBatchSize = searchReloadEntitiesBatchSize;
        this.processQueueBatchSize = processQueueBatchSize;
        this.maxProcessedQueueItemsPerExecution = maxProcessedQueueItemsPerExecution;
        this.reindexEntityEnqueueBatchSize = reindexEntityEnqueueBatchSize;
        this.autoMapIndexFileContent = autoMapIndexFileContent;
    }

    public int getEsSearchSize() {
        return esSearchSize;
    }

    public int getMaxSearchPageCount() {
        return maxSearchPageCount;
    }

    public int getSearchReloadEntitiesBatchSize() {
        return searchReloadEntitiesBatchSize;
    }

    public int getProcessQueueBatchSize() {
        return processQueueBatchSize;
    }

    public int getMaxProcessedQueueItemsPerExecution() {
        return maxProcessedQueueItemsPerExecution;
    }

    public int getReindexEntityEnqueueBatchSize() {
        return reindexEntityEnqueueBatchSize;
    }

    public boolean isAutoMapIndexFileContent() {
        return autoMapIndexFileContent;
    }
}
