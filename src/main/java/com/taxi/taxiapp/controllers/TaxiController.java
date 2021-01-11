package com.taxi.taxiapp.controllers;

import com.taxi.taxiapp.services.TaxiService;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/taxis")
@RestController
public class TaxiController {

    private final TaxiService taxiService;

    public TaxiController(TaxiService taxiService) {
        this.taxiService = taxiService;
    }

    @GetMapping
    public Flux<TaxiAvailableResponseDTO> getAvailableTaxis(@RequestParam("type") TaxiType taxiType, @RequestParam("latitude") Double latitude, @RequestParam("longitude") Double longitude, @RequestParam(value = "radius", defaultValue = "1") Double radius) {
        Flux<GeoResult<RedisGeoCommands.GeoLocation<String>>> availableTaxisFlux = taxiService.getAvailableTaxis(taxiType, latitude, longitude, radius);
        return availableTaxisFlux.map(r -> new TaxiAvailableResponseDTO(r.getContent().getName()));
    }

    @GetMapping("/{taxiId}/status")
    public Mono<TaxiStatusDTO> getTaxiStatus(@PathVariable("taxiId") String taxiId) {
        return taxiService.getTaxiStatus(taxiId).map(s -> new TaxiStatusDTO(taxiId, s));
    }

    @PutMapping("/{taxiId}/status")
    public Mono<TaxiStatusDTO> updateTaxiStatus(@PathVariable("taxiId") String taxiId, @RequestParam("status") TaxiStatus taxiStatus) {
        return taxiService.updateTaxiStatus(taxiId, taxiStatus).map(t -> new TaxiStatusDTO(t.getTaxiId(), t.getTaxiStatus()));
    }

    @PutMapping("/{taxiId}/location")
    public Mono<TaxiLocationUpdatedEventResponseDTO> updateLocation(@PathVariable("taxiId") String taxiId, @RequestBody LocationDTO locationDTO) {
        return taxiService.updateLocation(taxiId, locationDTO).map(t -> new TaxiLocationUpdatedEventResponseDTO(taxiId));
    }

    @PostMapping
    public Mono<TaxiRegisterEventResponseDTO> register(@RequestBody TaxiRegisterEventDTO taxiRegisterEventDTO) {
        return taxiService.register(taxiRegisterEventDTO).map(t -> new TaxiRegisterEventResponseDTO(t.getTaxiId()));
    }
}
