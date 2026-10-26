package com.dataremo.Data_Remo.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
public class SchemaFix {

    private final JdbcTemplate jdbcTemplate;

    public SchemaFix(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ajustarSchemaAtletas() {
        // pontos_totais
        jdbcTemplate.execute("""
            DO $$
            BEGIN
                IF NOT EXISTS (
                    SELECT 1
                    FROM information_schema.columns
                    WHERE table_name = 'atletas'
                      AND column_name = 'pontos_totais'
                ) THEN
                    ALTER TABLE atletas
                        ADD COLUMN pontos_totais DOUBLE PRECISION DEFAULT 0 NOT NULL;
                END IF;
            END$$;
        """);

        // total_treinos
        jdbcTemplate.execute("""
            DO $$
            BEGIN
                IF NOT EXISTS (
                    SELECT 1
                    FROM information_schema.columns
                    WHERE table_name = 'atletas'
                      AND column_name = 'total_treinos'
                ) THEN
                    ALTER TABLE atletas
                        ADD COLUMN total_treinos INTEGER DEFAULT 0 NOT NULL;
                END IF;
            END$$;
        """);

        // tempo_total_minutos
        jdbcTemplate.execute("""
            DO $$
            BEGIN
                IF NOT EXISTS (
                    SELECT 1
                    FROM information_schema.columns
                    WHERE table_name = 'atletas'
                      AND column_name = 'tempo_total_minutos'
                ) THEN
                    ALTER TABLE atletas
                        ADD COLUMN tempo_total_minutos INTEGER DEFAULT 0 NOT NULL;
                END IF;
            END$$;
        """);
    }
}