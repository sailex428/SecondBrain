package me.sailex.secondbrain.llm.player2.function_calling;

import io.github.sashirestela.openai.common.function.FunctionDef;
import me.sailex.secondbrain.common.NPCController;
import me.sailex.secondbrain.context.ContextProvider;
import me.sailex.secondbrain.database.resources.ResourcesProvider;
import me.sailex.secondbrain.llm.LLMClient;
import me.sailex.secondbrain.llm.function_calling.constant.Function;
import me.sailex.secondbrain.llm.openai.function_calling.OpenAiFunctionManager;

import java.util.List;

public class Player2FunctionManager extends OpenAiFunctionManager {

    private List<FunctionDef> rawFunctions;

    public Player2FunctionManager(
        ResourcesProvider resourcesProvider,
        NPCController controller,
        ContextProvider contextProvider,
        LLMClient llmClient
    ) {
        super(resourcesProvider, controller, contextProvider, llmClient);
    }

    @Override
    protected List<FunctionDef> createFunctions() {
        return List.of(
                //defineFunction(Function.Name.CHAT, Function.Description.CHAT, Chat.class),
                defineFunction(Function.Name.MOVE_TO_COORDINATES, Function.Description.MOVE_TO_COORDINATES, MoveToCoordinates.class),
                defineFunction(Function.Name.MOVE_TO_ENTITY, Function.Description.MOVE_TO_ENTITY, MoveToEntity.class),
                defineFunction(Function.Name.MOVE_AWAY, Function.Description.MOVE_AWAY, MoveAway.class),
                defineFunction(Function.Name.MINE_BLOCK, Function.Description.MINE_BLOCK, MineBlock.class),
                defineFunction(Function.Name.DROP_ITEM, Function.Description.DROP_ITEM, DropItem.class),
                defineFunction(Function.Name.ATTACK_ENTITY, Function.Description.ATTACK_ENTITY, AttackEntity.class),
                defineFunction(Function.Name.GET_ENTITIES, Function.Description.GET_ENTITIES, GetEntities.class),
                defineFunction(Function.Name.GET_BLOCKS, Function.Description.GET_BLOCKS, GetBlocks.class),
                //defineFunction(Function.Name.GET_RECIPES, Function.Description.GET_RECIPES, GetRecipes.class),
                //defineFunction(Function.Name.GET_CONVERSATIONS, Function.Description.GET_CONVERSATIONS, GetConversations.class),
                //embedding needed for these functions
                defineFunction(Function.Name.STOP, Function.Description.STOP, Stop.class)
        );
    }

    @Override
    public void vectorizeFunctions(List<FunctionDef> rawFunctions) {
        this.rawFunctions = rawFunctions;
    }

    @Override
    public List<FunctionDef> getRelevantFunctions(String prompt) {
        return rawFunctions; //provide all functions to the llm
    }


}
