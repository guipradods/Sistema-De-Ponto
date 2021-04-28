package com.example.demo.service;

import com.example.demo.model.Ponto;
import com.example.demo.repository.PontoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;

@Service
public class PontoService {

    @Autowired
    private PontoRepository pontoRepository;

    public Ponto registrarHora() {

        if (!checarDataNoBanco(LocalDate.now())) {
            pontoRepository.save(new Ponto(LocalDate.now()));
        }

        Ponto ponto = pontoRepository.findByDiaDoMes(LocalDate.now());

        if (ponto.getPontoUm() == null) {
            ponto.setPontoUm(LocalTime.now());
            pontoRepository.save(ponto);
        } else if (ponto.getPontoDois() == null) {
            ponto.setPontoDois(LocalTime.now());
            pontoRepository.save(ponto);
        } else if (ponto.getPontoTres() == null) {
            if (ponto.getPontoDois().plusHours(0).isBefore(LocalTime.now())) {
                ponto.setPontoTres(LocalTime.now());
                pontoRepository.save(ponto);
            } else {
                return ponto;
            }
        } else if (ponto.getPontoQuatro() == null) {
            ponto.setPontoQuatro(LocalTime.now());
            atualizarBancoDeHoras(ponto);
            pontoRepository.save(ponto);
        }

        return ponto;

    }

    public Boolean checarDataNoBanco(LocalDate data) {
        return pontoRepository.findByDiaDoMes(data) != null;
    }

    public Boolean checarDiaDaSemanaValido(LocalDate data) {
        if (data.getDayOfWeek() == DayOfWeek.of(6) || data.getDayOfWeek() == DayOfWeek.of(7)) {
            return false;
        }
        return true;
    }

    public void atualizarBancoDeHoras(Ponto ponto) {

        LocalTime registroUm = ponto.getPontoQuatro().minus(Duration.ofSeconds(ponto.getPontoTres().toSecondOfDay()));
        LocalTime registroDois = ponto.getPontoDois().minus(Duration.ofSeconds(ponto.getPontoUm().toSecondOfDay()));

        double bancoDeHoras = (double) (registroUm.plus(Duration.ofSeconds(registroDois.toSecondOfDay()))).getLong(ChronoField.SECOND_OF_DAY) / 3600;

        double bancoDeHorasFormatado = Math.round(bancoDeHoras * 100.0) / 100.0;

        if (pontoRepository.findFirstByOrderByDiaDoMesDesc() == null) {
            ponto.setBancoDeHoras(bancoDeHorasFormatado);
        } else {
            bancoDeHorasFormatado = bancoDeHorasFormatado + pontoRepository.findFirstByOrderByDiaDoMesDesc().getBancoDeHoras();
            ponto.setBancoDeHoras(bancoDeHorasFormatado);
        }

    }

}
