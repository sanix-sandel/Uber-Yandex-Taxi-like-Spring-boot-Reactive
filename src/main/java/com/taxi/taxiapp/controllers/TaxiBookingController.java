package com.taxi.taxiapp.controllers;

import org.springframework.web.bind.annotation.*;

@RequestMapping("/taxibookings")
@RestController
public class TaxiBookingController {

    private final TaxiBookingService taxiBookingService;

    public TaxiBookingController(TaxiBookingService taxiBookingService) {
        this.taxiBookingService = taxiBookingService;
    }

    @PostMapping
    public Mono<TaxiBookedEventResponseDTO> book(@RequestBody TaxiBookedEventDTO taxiBookedEventDTO) {
        return taxiBookingService.book(taxiBookedEventDTO).map(t -> new TaxiBookedEventResponseDTO(t.getTaxiBookingId()));
    }

    @PutMapping("/{taxiBookingId}/cancel")
    public Mono<TaxiBookingCanceledEventResponseDTO> cancel(@PathVariable("taxiBookingId") String taxiBookingId, @RequestBody TaxiBookingCanceledEventDTO taxiBookingCanceledEventDTO) {
        return taxiBookingService.cancel(taxiBookingId, taxiBookingCanceledEventDTO).map(t -> new TaxiBookingCanceledEventResponseDTO(t.getTaxiBookingId()));
    }

    @PutMapping("/{taxiBookingId}/accept")
    public Mono<TaxiBookingAcceptedEventResponseDTO> accept(@PathVariable("taxiBookingId") String taxiBookingId, @RequestBody TaxiBookingAcceptedEventDTO taxiBookingAcceptedEventDTO) {
        return taxiBookingService.accept(taxiBookingId, taxiBookingAcceptedEventDTO).map(t -> new TaxiBookingAcceptedEventResponseDTO(t.getTaxiBookingId(), t.getTaxiId(), t.getAcceptedTime()));
    }

    @GetMapping
    public Flux<TaxiBookingResponseDTO> getBookings(@RequestParam("type") TaxiType taxiType, @RequestParam("latitude") Double latitude, @RequestParam("longitude") Double longitude, @RequestParam(value = "radius", defaultValue = "1") Double radius) {
        return taxiBookingService.getBookings(taxiType, latitude, longitude, radius).map(r -> new TaxiBookingResponseDTO(r.getContent().getName()));
    }
}
