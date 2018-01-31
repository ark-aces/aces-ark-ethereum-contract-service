package com.arkaces.ark_ethereum_contract_service.contract;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface ContractRepository extends PagingAndSortingRepository<ContractEntity, Long> {

    ContractEntity findOneById(String id);

    ContractEntity findOneByArkSmartBridge(String arkSmartBridge);
}
