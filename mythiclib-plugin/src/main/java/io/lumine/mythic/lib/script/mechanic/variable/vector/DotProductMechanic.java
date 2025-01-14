package io.lumine.mythic.lib.script.mechanic.variable.vector;

import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.script.variable.def.DoubleVariable;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.util.Position;
import org.apache.commons.lang.Validate;

@MechanicMetadata
public class DotProductMechanic extends VariableMechanic {
    private final String varName1, varName2;

    public DotProductMechanic(ConfigObject config) {
        super(config);

        // Term by term addition
        config.validateKeys("vec1", "vec2");
        varName1 = config.getString("vec1");
        varName2 = config.getString("vec2");
    }

    @Override
    public void cast(SkillMetadata meta) {

        Variable var1 = meta.getVariable(varName1);
        Validate.isTrue(var1 instanceof PositionVariable, "Variable '" + varName1 + "' is not a vector");
        Position vec1 = (Position) var1.getStored();

        Variable var2 = meta.getVariable(varName2);
        Validate.isTrue(var2 instanceof PositionVariable, "Variable '" + varName2 + "' is not a vector");
        Position vec2 = (Position) var2.getStored();

        getTargetVariableList(meta).registerVariable(new DoubleVariable(getVariableName(), vec1.dot(vec2)));
    }
}
