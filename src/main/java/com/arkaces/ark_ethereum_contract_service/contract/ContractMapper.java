package com.arkaces.ark_ethereum_contract_service.contract;

import com.arkaces.aces_server.aces_service.contract.Contract;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ContractMapper {

    private final ModelMapper modelMapper;

    public Contract<Results> map(ContractEntity contractEntity) {
        Contract<Results> contract = new Contract<>();
        modelMapper.map(contractEntity, contract);

        Results results = new Results();
        modelMapper.map(contractEntity, results);
        contract.setResults(results);
        
        return contract;
    }
}
