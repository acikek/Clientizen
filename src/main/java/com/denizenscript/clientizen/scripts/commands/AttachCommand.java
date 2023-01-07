package com.denizenscript.clientizen.scripts.commands;

import com.denizenscript.clientizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgLinear;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;

import java.util.*;

public class AttachCommand extends AbstractCommand {

	public static Map<UUID, List<EntityTag>> attachMap = new HashMap<>();
	public static List<UUID> attachedEntities = new ArrayList<>();

	public AttachCommand() {
		setName("attach");
		setSyntax("attach [<entity>|...] [to:<entity>] (cancel)");
		setRequiredArguments(2, 2);
		isProcedural = false;
		autoCompile();
	}

	public static void autoExecute(ScriptEntry scriptEntry,
								   @ArgLinear @ArgName("entities") ListTag attachingEntities,
								   @ArgDefaultNull @ArgPrefixed @ArgName("to") EntityTag toEntity,
								   @ArgName("cancel") boolean cancel) {
		if (!cancel && toEntity == null) {
			throw new InvalidArgumentsRuntimeException("Must specify an entity to attach to");
		}
		List<EntityTag> attaching = attachingEntities.filter(EntityTag.class, scriptEntry.context);
		if (attachMap.containsKey(toEntity.uuid)) {
			attachMap.get(toEntity.uuid).addAll(attaching);
		}
		else {
			attachMap.put(toEntity.uuid, attaching);
		}
		for (EntityTag entityTag : attaching) {
			attachedEntities.add(entityTag.uuid);
		}
	}
}