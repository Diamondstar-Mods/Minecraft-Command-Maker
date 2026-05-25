import os
import re

base = 'server-1.18-1.19-1.20/src/main/java'

# Fix all .java files
for root, dirs, files in os.walk(base):
    for fname in files:
        if not fname.endswith('.java'):
            continue
        path = os.path.join(root, fname)
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()

        changed = False

        # 1. Identifier.of("x", "y") -> new Identifier("x", "y")
        new_content = re.sub(r'Identifier\.of\(', 'new Identifier(', content)
        if new_content != content:
            changed = True
            content = new_content

        # 2. Remove DataComponentTypes import
        if 'import net.minecraft.component.DataComponentTypes;' in content:
            content = content.replace('import net.minecraft.component.DataComponentTypes;\n', '')
            changed = True

        # 3. stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(X)) -> stack.setCustomName(Text.literal(X))
        new_content = re.sub(
            r'stack\.set\(DataComponentTypes\.CUSTOM_NAME,\s*Text\.literal\(([^)]+)\)\)',
            r'stack.setCustomName(Text.literal(\1))',
            content
        )
        if new_content != content:
            changed = True
            content = new_content

        # 4. ScreenHandlerType with FeatureSet -> without
        if 'net.minecraft.resource.featuretoggle.FeatureSet.empty()' in content:
            content = content.replace(
                'new ScreenHandlerType<FunctionChestHandler>(FunctionChestHandler::new, net.minecraft.resource.featuretoggle.FeatureSet.empty())',
                'new ScreenHandlerType<FunctionChestHandler>(FunctionChestHandler::new)'
            )
            changed = True

        if changed:
            with open(path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f'Fixed: {path}')
