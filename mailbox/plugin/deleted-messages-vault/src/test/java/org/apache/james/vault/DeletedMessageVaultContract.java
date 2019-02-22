/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.vault;

import static org.apache.james.vault.DeletedMessageFixture.DELETED_MESSAGE;
import static org.apache.james.vault.DeletedMessageFixture.DELETED_MESSAGE_2;
import static org.apache.james.vault.DeletedMessageFixture.MESSAGE_ID;
import static org.apache.james.vault.DeletedMessageFixture.USER;
import static org.apache.james.vault.DeletedMessageFixture.USER_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeletedMessageVaultContract {
    DeletedMessageVault getVault();

    @Test
    default void searchAllShouldThrowOnNullUser() {
       assertThatThrownBy(() -> getVault().search(null, Query.all()))
           .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void searchAllShouldThrowOnNullQuery() {
       assertThatThrownBy(() -> getVault().search(USER, null))
           .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void appendShouldThrowOnNullMessage() {
       assertThatThrownBy(() -> getVault().append(USER, null))
           .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void appendShouldThrowOnNullUser() {
       assertThatThrownBy(() -> getVault().append(null, DELETED_MESSAGE))
           .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void deleteShouldThrowOnNullMessageId() {
       assertThatThrownBy(() -> getVault().delete(null, MESSAGE_ID))
           .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void deleteShouldThrowOnNullUser() {
       assertThatThrownBy(() -> getVault().delete(USER, null))
           .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void searchAllShouldReturnEmptyWhenNoItem() {
        assertThat(Flux.from(getVault().search(USER, Query.all())).collectList().block())
            .isEmpty();
    }

    @Test
    default void searchAllShouldReturnContainedItems() {
        Mono.from(getVault().append(USER, DELETED_MESSAGE)).block();

        assertThat(Flux.from(getVault().search(USER, Query.all())).collectList().block())
            .containsOnly(DELETED_MESSAGE);
    }

    @Test
    default void searchAllShouldReturnAllContainedItems() {
        Mono.from(getVault().append(USER, DELETED_MESSAGE)).block();
        Mono.from(getVault().append(USER, DELETED_MESSAGE_2)).block();

        assertThat(Flux.from(getVault().search(USER, Query.all())).collectList().block())
            .containsOnly(DELETED_MESSAGE, DELETED_MESSAGE_2);
    }

    @Test
    default void vaultShouldBePartitionnedByUser() {
        Mono.from(getVault().append(USER, DELETED_MESSAGE)).block();

        assertThat(Flux.from(getVault().search(USER_2, Query.all())).collectList().block())
            .isEmpty();
    }

    @Test
    default void searchAllShouldNotReturnDeletedItems() {
        Mono.from(getVault().append(USER, DELETED_MESSAGE)).block();

        Mono.from(getVault().delete(USER, MESSAGE_ID)).block();

        assertThat(Flux.from(getVault().search(USER, Query.all())).collectList().block())
            .isEmpty();
    }
}
