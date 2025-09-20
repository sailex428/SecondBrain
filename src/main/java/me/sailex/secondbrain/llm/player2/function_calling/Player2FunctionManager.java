package me.sailex.secondbrain.llm.player2.function_calling;

import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.Functional;
import me.sailex.altoclef.AltoClefController;
import me.sailex.secondbrain.llm.function_calling.constant.Function;
import me.sailex.secondbrain.llm.openai.function_calling.OpenAiFunctionManager;
import me.sailex.secondbrain.llm.player2.converter.Player2SchemaConverter;

import java.util.List;

public class Player2FunctionManager extends OpenAiFunctionManager {

    public Player2FunctionManager(AltoClefController controller) {
        super(controller);
    }

    protected List<FunctionDef> createFunctions() {
        return List.of(
//        defineFunction(Function.Name.GET_ITEMS, Function.Description.GET_ITEMS, MoveToCoordinates.class),
//        defineFunction(Function.Name.MOVE_TO_ENTITY, Function.Description.MOVE_TO_ENTITY, MoveToEntity.class),
//        defineFunction(Function.Name.MOVE_AWAY, Function.Description.MOVE_AWAY, MoveAway.class),
//        defineFunction(Function.Name.MINE_BLOCK, Function.Description.MINE_BLOCK, MineBlock.class),
//        defineFunction(Function.Name.DROP_ITEM, Function.Description.DROP_ITEM, DropItem.class),
//        defineFunction(Function.Name.ATTACK_ENTITY, Function.Description.ATTACK_ENTITY, AttackEntity.class),
//        defineFunction(Function.Name.GET_ENTITIES, Function.Description.GET_ENTITIES, GetEntities.class),
//        defineFunction(Function.Name.GET_BLOCKS, Function.Description.GET_BLOCKS, GetBlocks.class),
//        defineFunction(Function.Name.STOP, Function.Description.STOP, Stop.class)
        );
    }

    @Override
    public <T extends Functional> FunctionDef defineFunction(String name, String description, Class<T> clazz) {
        return FunctionDef.builder()
                .name(name)
                .description(description)
                .functionalClass(clazz)
                .schemaConverter(new Player2SchemaConverter())
                .strict(true)
                .build();
    }
}
