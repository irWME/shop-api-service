package nl.simpliphi.shopapiservice.customer;

import lombok.extern.slf4j.Slf4j;
import nl.simpliphi.shopprojections.customer.CustomerDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/find")
    public ResponseEntity<SearchHits<CustomerDto>> findCustomers(
            @RequestParam(required = false) MultiValueMap<String, String> params,
            @SortDefault.SortDefaults({@SortDefault(sort = {"lastName.keyword"}, direction = Sort.Direction.DESC)}) Pageable pageable
    ) {
        return ResponseEntity.ok().body(customerService.findCustomers(params, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable String id) {
        return ResponseEntity.ok().body(customerService.getCustomer(id));
    }

    @PostMapping("create/{id}/{firstName}/{lastName}")
    public ResponseEntity<String> createCustomer(@PathVariable String id, @PathVariable String firstName, @PathVariable String lastName) {
        return ResponseEntity.accepted().body("Triggered command: " + customerService.createCustomer(id, firstName, lastName));
    }

    @GetMapping
    public ResponseEntity<String> getMessage() {
        return ResponseEntity.ok().body(Instant.now() + " | Hi demo!");
    }

    @GetMapping("/exception")
    public ResponseEntity<String> exception() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request because a terrible validation exception occurred...", new RuntimeException());
    }
}