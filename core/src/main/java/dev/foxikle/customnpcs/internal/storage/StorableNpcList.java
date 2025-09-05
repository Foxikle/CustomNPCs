package dev.foxikle.customnpcs.internal.storage;

import lombok.Data;

import java.util.List;

@Data
public class StorableNpcList {
    List<StorableNPC> npcs;
}
