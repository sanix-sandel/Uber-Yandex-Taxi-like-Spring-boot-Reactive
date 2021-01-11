package com.taxi.taxiapp.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("Taxi")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Taxi implements Serializable {
    @Id
    private String taxiType;

    private TaxiType taxiType;

    private TaxiStatus taxiStatus;
}
