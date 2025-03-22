package com.helpbus.HelpBus.model.enums;

import lombok.Getter;

@Getter
public enum Cargo {
    CLIENTE(1),
    LIMPEZA(2),
    MANUTENCAO(3),
    ADMINISTRACAO(4),
    GERENTE(5);

    private final int id;

    Cargo(int id) {
        this.id = id;
    }

    public static Cargo fromId(int id) {
        for (Cargo cargo : Cargo.values()) {
            if (cargo.getId() == id) {
                return cargo;
            }
        }
        return null;
    }
}