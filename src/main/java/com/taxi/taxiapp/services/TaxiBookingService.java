package com.taxi.taxiapp.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.taxiapp.data.TaxiBooking;
import com.taxi.taxiapp.exceptions.TaxiBookingIdNotFoundException;
import com.taxi.taxiapp.repository.TaxiBookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Service
public class TaxiBookingService {

    private final static Logger LOGGER= LoggerFactory.getLogger(TaxiBookingService.class);
    private final ReactiveRedisTemplate<String, String>
            reactiveRedisTemplate;
    private final TaxiBookingRepository taxiBookingRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LocationToPointConverter locationToPointConverter = new
            LocationToPointConverter();

    public TaxiBookingService(ReactiveRedisTemplate<String, String> reactiveRedisTemplate, TaxiBookingRepository taxiBookingRepository) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.taxiBookingRepository = taxiBookingRepository;
    }

    public Mono<TaxiBooking> book(TaxiBookedEventDTO taxiBookedEventDTO){
        TaxiBooking taxiBooking=new TaxiBooking();
        taxiBooking.setEnd(locationToPointConverter.convert(taxiBookedEventDTO.getEnd()));
        taxiBooking.setStart(locationToPointConverter.convert(taxiBookedEventDTO.getStart()));
        taxiBooking.setBookedTime(taxiBookedEventDTO.getBookedTime());
        taxiBooking.setCustomerId(taxiBookedEventDTO.getCustomerId());
        taxiBooking.setBookingStatus(TaxiBookingStatus.ACTIVE);
        TaxiBooking savedTaxiBooking =
                taxiBookingRepository.save(taxiBooking);
        return reactiveRedisTemplate.opsForGeo().add(getTaxiTypeBookings(taxiBookedEventDTO.getTaxiType()), taxiBooking.getStart(),
                        taxiBooking.getTaxiBookingId()).flatMap(l -> Mono.just(savedTaxiBooking));
    }//allow the passenger to save a booking

    public Mono<TaxiBooking> cancel(String taxiBookingId, TaxiBookingCanceledEventDTO canceledEventDTO){
        Optional<TaxiBooking> taxiBookingOptional =
                taxiBookingRepository.findById(taxiBookingId);
        if (taxiBookingOptional.isPresent()) {
            TaxiBooking taxiBooking = taxiBookingOptional.get();
            taxiBooking.setBookingStatus(TaxiBookingStatus.CANCELLED);
            taxiBooking.setReasonToCancel(canceledEventDTO.getReason());
            taxiBooking.setCancelTime(canceledEventDTO.getCancelTime());
            return Mono.just(taxiBookingRepository.save(taxiBooking));
        } else {
            throw getTaxiBookingIdNotFoundException(taxiBookingId);
        }
    }//cancel a booking

    public Mono<TaxiBooking> accept (String taxiBookingId, TaxiBookingAcceptedEventDTO acceptedEventDTO){
        Optional<TaxiBooking> taxiBookingOptional = taxiBookingRepository.findById(taxiBookingId);
        if (taxiBookingOptional.isPresent()) {
            TaxiBooking taxiBooking = taxiBookingOptional.get();
            taxiBooking.setTaxiId(acceptedEventDTO.getTaxiId());
            taxiBooking.setAcceptedTime(acceptedEventDTO.getAcceptedTime());
            return Mono.just(taxiBookingRepository.save(taxiBooking)).doOnSuccess(t -> {
                try {
                    redisTemplate.convertAndSend(RedisConfig.ACCEPTED_EVENT_CHANNEL, objectMapper.writeValueAsString(acceptedEventDTO));
                } catch (JsonProcessingException e) {
                    LOGGER.error("Error while sending message to Channel {}", RedisConfig.ACCEPTED_EVENT_CHANNEL, e);
                }
            });
        } else {
            throw getTaxiBookingIdNotFoundException(taxiBookingId);
        }

    }

    ublic Flux<GeoResult<RedisGeoCommands.GeoLocation<String>>> getBookings(TaxiType taxiType, Double latitude, Double longitude, Double radius) {
        return reactiveRedisTemplate.opsForGeo().radius(getTaxiTypeBookings(taxiType), new Circle(new Point(longitude, latitude), new Distance(radius, Metrics.KILOMETERS)));
    }

    public Mono<TaxiBooking> updateBookingStatus(String taxiBookingId, TaxiBookingStatus taxiBookingStatus) {
        Optional<TaxiBooking> taxiBookingOptional = taxiBookingRepository.findById(taxiBookingId);
        if (taxiBookingOptional.isPresent()) {
            TaxiBooking taxiBooking = taxiBookingOptional.get();
            taxiBooking.setBookingStatus(taxiBookingStatus);
            return Mono.just(taxiBookingRepository.save(taxiBooking));
        } else {
            throw getTaxiBookingIdNotFoundException(taxiBookingId);
        }
    }

    private TaxiBookingIdNotFoundException getTaxiBookingIdNotFoundException(String taxiBookingId) {
        return new TaxiBookingIdNotFoundException("Taxi Booking Id "+taxiBookingId+" Not Found");
    }

    private String getTaxiTypeBookings(TaxiType taxiType) {
        return taxiType.toString()+"-Bookings";
    }
}
