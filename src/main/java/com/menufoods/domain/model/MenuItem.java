package com.menufoods.domain.model;

import com.menufoods.domain.enums.MenuItemType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
@Entity(name = "itens_cardapio")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "nome", unique = true)
    private String name;

    @Column(name = "descricao")
    private String description;

    @NotNull
    @Column(name = "tipo")
    private MenuItemType type;

    @Column
    private String photoUrl;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "menuItems")
    private List<Ingredient> ingredients = new ArrayList<>();

}
