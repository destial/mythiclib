package io.lumine.mythic.lib.skill.mechanic.variable.vector;

import io.lumine.mythic.lib.util.ConfigObject;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.skill.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.skill.variable.Variable;
import io.lumine.mythic.lib.skill.variable.def.PositionVariable;
import io.lumine.mythic.lib.util.Position;
import org.apache.commons.lang.Validate;

@MechanicMetadata
public class SubtractVectorMechanic extends VariableMechanic {
    private final DoubleFormula x, y, z;
    private final String varToAdd;

    public SubtractVectorMechanic(ConfigObject config) {
        super(config);

        // Term by term addition
        x = config.contains("x") ? new DoubleFormula(config.getString("x")) : DoubleFormula.ZERO;
        y = config.contains("y") ? new DoubleFormula(config.getString("y")) : DoubleFormula.ZERO;
        z = config.contains("z") ? new DoubleFormula(config.getString("z")) : DoubleFormula.ZERO;

        // Vector addition
        varToAdd = config.getString("subtracted", null);
    }

    @Override
    public void cast(SkillMetadata meta) {

        Variable targetVar = meta.getVariable(getVariableName());
        Validate.isTrue(targetVar instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        Position target = (Position) targetVar.getStored();

        // Vector addition
        if (varToAdd != null) {
            Variable var = meta.getVariable(varToAdd);
            Validate.isTrue(var instanceof PositionVariable, "Variable '" + varToAdd + "' is not a vector");
            target.add(((PositionVariable) var).getStored().clone().multiply(-1));
        }

        // Term by term addition
        double x = this.x.evaluate(meta);
        double y = this.y.evaluate(meta);
        double z = this.z.evaluate(meta);

        target.add(-x, -y, -z);
    }
}
