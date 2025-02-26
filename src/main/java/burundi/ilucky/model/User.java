package burundi.ilucky.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Entity
@Table(name = "_user")
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date addTime;
    
    private long totalPlay;

    private long totalVnd;

    private long totalStar;

}
