/*
 * Copyright 2021 Haulmont.
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

package test_support.repository;

import io.jmix.core.repository.UnsafeDataRepository;
import test_support.entity.TestOrder;

import java.util.List;

@UnsafeDataRepository
public interface ChildUnsafeRepository extends ParentUnsafeRepository {

    @Override
    List<TestOrder> findOrdersByNumberNotNull();

    List<TestOrder> findAll();
}
