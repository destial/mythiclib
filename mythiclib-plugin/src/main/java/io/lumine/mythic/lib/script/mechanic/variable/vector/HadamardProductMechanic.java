package io.lumine.mythic.lib.script.mechanic.variable.vector;

import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.util.Position;
import org.apache.commons.lang.Validate;

@MechanicMetadata
public class HadamardProductMechanic extends VariableMechanic {
    private final String varName1, varName2;

    public HadamardProductMechanic(ConfigObject config) {
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
        Position pos1 = (Position) var1.getStored();

        Variable var2 = meta.getVariable(varName2);
        Validate.isTrue(var2 instanceof PositionVariable, "Variable '" + varName2 + "' is not a vector");
        Position pos2 = (Position) var2.getStored();

        getTargetVariableList(meta).registerVariable(new PositionVariable(getVariableName(), hadamardProduct(pos1, pos2)));
    }

    /**
     * In mathematics, the Hadamard product is a binary operation that takes
     * two matrices of the same dimensions and produces another matrix of the
     * same dimension as the operands, where each element i, j is the product
     * of elements i, j of the original two matrices. It is to be distinguished
     * from the more common matrix product.
     */
    private Position hadamardProduct(Position pos1, Position pos2) {
        return new Position(pos1.getWorld(), pos1.getX() * pos2.getX(), pos1.getY() * pos2.getY(), pos1.getZ() * pos2.getZ());
    }
}
