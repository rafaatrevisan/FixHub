package com.helpbus.HelpBus.model.enums;

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

    public int getId() {
        return id;
    }
}
