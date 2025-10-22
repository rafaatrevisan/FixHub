package com.fixhub.FixHub.model.enums;

public enum Cargo {
    CLIENTE(1),
    LIMPEZA(2),
    MANUTENCAO(3),
    SUPORTE(4),
    GERENTE(5);

    private final int id;

    Cargo(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
