package nl.simpliphi.shopapiservice.customer;

import com.github.easysourcing.messages.commands.CommandGateway;
import lombok.extern.slf4j.Slf4j;
import nl.simpliphi.shopdomain.customer.CustomerCommand;
import nl.simpliphi.shopprojections.customer.CustomerDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
public class CustomerService {

    private final CommandGateway commandGateway;
    private final CustomerRepository customerRepository;

    public CustomerService(CommandGateway commandGateway, CustomerRepository customerRepository) {
        this.commandGateway = commandGateway;
        this.customerRepository = customerRepository;
    }

    public SearchHits<CustomerDto> findCustomers(MultiValueMap<String, String> params, Pageable pageable) {
        SearchHits<CustomerDto> search = customerRepository.search(params, pageable);
        return search;
    }

    public CustomerDto getCustomer(String id) {
        return customerRepository.getById(id);
    }

    public CustomerCommand.CreateCustomer createCustomer(String id, String firstName, String lastName) {
        CustomerCommand.CreateCustomer command = CustomerCommand.CreateCustomer.builder()
                .customerId(id)
                .firstName(firstName)
                .lastName(lastName)
                .build();
        commandGateway.send(command);

        return command;
    }
}