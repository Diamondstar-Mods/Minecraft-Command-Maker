"""Adapt server-1.17 source from 1.21 APIs to 1.17 APIs."""
import os, re

BASE = 'server-1.17/src/main/java'

def fix_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    original = content

    # 1. Identifier.of("a","b") -> new Identifier("a","b")
    content = re.sub(r'Identifier\.of\(', 'new Identifier(', content)

    # 2. DataComponentTypes -> setCustomName (1.17 has setCustomName)
    content = re.sub(
        r'stack\.set\(DataComponentTypes\.CUSTOM_NAME,\s*Text\.literal\(([^)]+)\)\)',
        r'stack.setCustomName(Text.literal(\1))',
        content
    )

    # 3. Remove DataComponentTypes import
    content = content.replace('import net.minecraft.component.DataComponentTypes;\n', '')

    # 4. ScreenHandlerType with FeatureSet -> without
    content = content.replace(
        'new ScreenHandlerType<FunctionChestHandler>(FunctionChestHandler::new, net.minecraft.resource.featuretoggle.FeatureSet.empty())',
        'new ScreenHandlerType<FunctionChestHandler>(FunctionChestHandler::new)'
    )
    content = content.replace(
        'new ScreenHandlerType<FunctionChestHandler>(FunctionChestHandler::new, net.minecraft.resource.featuretoggle.FeatureSet.empty())',
        'new ScreenHandlerType<FunctionChestHandler>(FunctionChestHandler::new)'
    )

    # 5. v2 command API -> v1
    content = content.replace(
        'import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;',
        'import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;'
    )
    # v2 callback signature: (dispatcher, registryAccess, environment) -> (dispatcher, dedicated)
    content = content.replace(
        'CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {',
        'CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {'
    )

    # 6. Registries -> Registry (old package)
    content = content.replace(
        'import net.minecraft.registry.Registries;',
        'import net.minecraft.util.registry.Registry;'
    )
    content = content.replace(
        'import net.minecraft.registry.Registry;',
        ''
    )
    content = content.replace('Registry.register(Registries.SCREEN_HANDLER,', 'Registry.register(Registry.SCREEN_HANDLER,')

    # 7. sendFeedback(() -> Text.literal(...), false) -> sendFeedback(Text.literal(...), false)
    # Pattern: .sendFeedback(() -> Text.literal(  ...  ), false)
    content = re.sub(
        r'\.sendFeedback\(\(\)\s*->\s*(Text\.literal\([^)]+\))\s*,\s*false\)',
        r'.sendFeedback(\1, false)',
        content
    )

    # 8. DrawContext -> MatrixStack in Screen subclasses
    content = content.replace(
        'import net.minecraft.client.gui.DrawContext;',
        'import net.minecraft.client.util.math.MatrixStack;'
    )

    # render(DrawContext context, int mouseX, int mouseY, float delta)
    content = re.sub(
        r'void render\(DrawContext\s+(\w+),\s*int\s+(\w+),\s*int\s+(\w+),\s*float\s+(\w+)\)',
        r'void render(MatrixStack \1, int \2, int \3, float \4)',
        content
    )
    # renderBackground(DrawContext context, ...)
    content = re.sub(
        r'renderBackground\(DrawContext\s+(\w+),',
        r'renderBackground(MatrixStack \1,',
        content
    )
    # super.render(DrawContext context, ...)
    content = re.sub(
        r'super\.render\(DrawContext\s+(\w+),',
        r'super.render(MatrixStack \1,',
        content
    )

    # context.fill -> fill(matrices, (static method in Screen)
    content = re.sub(
        r'(\s+)context\.fill\(([^)]+)\);',
        r'\1fill(matrices, \2);',
        content
    )

    # context.drawText(textRenderer, ...) -> textRenderer.draw(matrices, ...)
    content = re.sub(
        r'context\.drawText\(this\.textRenderer,\s*',
        r'this.textRenderer.draw(matrices, ',
        content
    )

    # context.drawCenteredText -> textRenderer.draw(matrices, ...)
    content = re.sub(
        r'context\.drawCenteredText\(this\.textRenderer,\s*',
        r'drawCenteredText(matrices, this.textRenderer, ',
        content
    )

    # drawBackground(DrawContext -> drawBackground(MatrixStack
    content = re.sub(
        r'drawBackground\(DrawContext\s+(\w+),',
        r'drawBackground(MatrixStack \1,',
        content
    )

    # drawForeground(DrawContext -> drawForeground(MatrixStack
    content = re.sub(
        r'drawForeground\(DrawContext\s+(\w+),',
        r'drawForeground(MatrixStack \1,',
        content
    )

    # drawMouseoverTooltip(DrawContext -> drawMouseoverTooltip(MatrixStack
    content = re.sub(
        r'drawMouseoverTooltip\(DrawContext\s+(\w+),',
        r'drawMouseoverTooltip(MatrixStack \1,',
        content
    )

    # context.drawTooltip -> renderTooltip(matrices,
    content = re.sub(
        r'context\.drawTooltip\(this\.textRenderer,\s*',
        r'renderTooltip(matrices, ',
        content
    )

    # 9. renderBackground signature change
    content = content.replace(
        'this.renderBackground(context, mouseX, mouseY, delta);',
        'this.renderBackground(matrices);'
    )

    # 10. Fix HandledScreen imports for MatrixStack
    content = content.replace(
        'HandledScreen<FunctionChestHandler>',
        'HandledScreen<FunctionChestHandler>'
    )

    # 11. Fix lone Registry import (already handled above)
    # Clean up double blank lines from removed imports
    content = re.sub(r'\n{3,}', '\n\n', content)

    if content != original:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        return True
    return False

count = 0
for root, dirs, files in os.walk(BASE):
    for fname in files:
        if fname.endswith('.java'):
            path = os.path.join(root, fname)
            if fix_file(path):
                count += 1
                print(f'Fixed: {path}')

print(f'\nDone. {count} files modified.')
