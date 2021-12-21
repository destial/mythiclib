package io.lumine.mythic.lib.skill.variable.def;

import io.lumine.mythic.lib.skill.variable.Variable;
import io.lumine.mythic.lib.skill.variable.VariableMetadata;
import io.lumine.mythic.lib.skill.variable.VariableRegistry;

@VariableMetadata(name = "double")
public class DoubleVariable extends Variable<Double> {
    public static final VariableRegistry<DoubleVariable> VARIABLE_REGISTRY = new VariableRegistry();

    static {
        VARIABLE_REGISTRY.registerVariable("int", var -> new IntegerVariable("temp", (int) (double) var.getStored()));
    }

    public DoubleVariable(String name, double value) {
        super(name, value);
    }

    @Override
    public VariableRegistry getVariableRegistry() {
        return VARIABLE_REGISTRY;
    }
}