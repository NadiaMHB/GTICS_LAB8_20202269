package org.example.lab820202269.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "reserva")
@Getter
@Setter
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idreserva")
    private int id;

    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Column(name = "correo", nullable = false, length = 45)
    private String correo;

    @Column(name = "cuposReserva", nullable = false)
    private int cuposReserva;

    @ManyToOne
    @JoinColumn(name = "evento_idevento", nullable = false)
    @JsonBackReference
    private Evento evento;
}
