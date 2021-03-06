package org.dhis2.utils;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.List;
import java.util.Map;

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

public interface RulesUtilsProvider {


    void applyRuleEffects(Map<String, FieldViewModel> fieldViewModels, Result<RuleEffect> calcResult, RulesActionCallbacks rulesActionCallbacks);

    void applyRuleEffects( Map<String, ProgramStageModel> programStages, Result<RuleEffect> calcResult);

}
