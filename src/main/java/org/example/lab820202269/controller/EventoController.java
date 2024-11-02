package org.example.lab820202269.controller;

import org.example.lab820202269.entity.Evento;
import org.example.lab820202269.entity.Reserva;
import org.example.lab820202269.repository.EventoRepository;
import org.example.lab820202269.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @GetMapping
    public ResponseEntity<List<Evento>> listarEventos(@RequestParam(required = false)  @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fecha ) {
        List<Evento> resultado;
        if (fecha != null) {
            resultado = eventoRepository.findAll().stream()
                    .filter(evento -> evento.getFecha().equals(fecha))
                    .toList();
        } else {
            resultado = eventoRepository.findAll();
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> crearEvento(@RequestBody Evento evento) {
        HashMap<String, Object> responseJson = new HashMap<>();

        List<String> categoriasPermitidas = Arrays.asList("Conferencias", "Exposiciones", "Talleres", "Conciertos");

        if (!categoriasPermitidas.contains(evento.getCategoria())) {
            responseJson.put("result", "error");
            responseJson.put("msg", "Categoría no válida. Las categorías permitidas son: " + categoriasPermitidas);
            return ResponseEntity.badRequest().body(responseJson);
        }

        if (evento.getFecha().isBefore(LocalDate.now())) {
            responseJson.put("result", "error");
            responseJson.put("msg", "La fecha debe ser futura.");
            return ResponseEntity.badRequest().body(responseJson);
        }

        evento.setCantReservasActuales(0);
        Evento nuevoEvento = eventoRepository.save(evento);

        responseJson.put("result", "success");
        responseJson.put("evento", nuevoEvento);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseJson);
    }

    @PostMapping("/reservas")
    public ResponseEntity<HashMap<String, Object>> reservarLugar(@RequestBody Reserva reserva) {
        HashMap<String, Object> responseJson = new HashMap<>();

        if (reserva.getEvento() == null || reserva.getEvento().getId() == 0) {
            responseJson.put("result", "error");
            responseJson.put("msg", "El campo 'evento' es obligatorio y debe tener un 'id'.");
            return ResponseEntity.badRequest().body(responseJson);
        }

        Optional<Evento> optEvento = eventoRepository.findById(reserva.getEvento().getId());

        if (optEvento.isPresent()) {
            Evento evento = optEvento.get();
            if (evento.getCantReservasActuales() + reserva.getCuposReserva() > evento.getMaxCapacidad()) {
                responseJson.put("result", "error");
                responseJson.put("msg", "No hay cupos disponibles.");
                return ResponseEntity.badRequest().body(responseJson);
            }

            evento.setCantReservasActuales(evento.getCantReservasActuales() + reserva.getCuposReserva());
            reserva.setEvento(evento);
            reservaRepository.save(reserva);
            eventoRepository.save(evento);

            responseJson.put("result", "success");
            responseJson.put("msg", "Reserva realizada con éxito.");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseJson);
        } else {
            responseJson.put("result", "error");
            responseJson.put("msg", "Evento no encontrado.");
            return ResponseEntity.badRequest().body(responseJson);
        }
    }

    @DeleteMapping("/reservas/{id}")
    public ResponseEntity<HashMap<String, Object>> cancelarReserva(@PathVariable Integer id) {
        HashMap<String, Object> responseJson = new HashMap<>();

        Optional<Reserva> optReserva = reservaRepository.findById(id);

        if (optReserva.isPresent()) {
            Reserva reserva = optReserva.get();
            Evento evento = reserva.getEvento();

            evento.setCantReservasActuales(evento.getCantReservasActuales() - reserva.getCuposReserva());
            eventoRepository.save(evento);
            reservaRepository.delete(reserva);

            responseJson.put("result", "success");
            responseJson.put("msg", "Reserva cancelada con éxito.");
            return ResponseEntity.ok(responseJson);
        } else {
            responseJson.put("result", "error");
            responseJson.put("msg", "Reserva no encontrada.");
            return ResponseEntity.badRequest().body(responseJson);
        }
    }
}
