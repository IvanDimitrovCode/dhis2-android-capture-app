package org.dhis2.utils;

import android.support.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleActionCreateEvent;
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair;
import org.hisp.dhis.rules.models.RuleActionDisplayText;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideProgramStage;
import org.hisp.dhis.rules.models.RuleActionHideSection;
import org.hisp.dhis.rules.models.RuleActionSetMandatoryField;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

public class RulesUtilsProviderImpl implements RulesUtilsProvider {

    private final CodeGenerator codeGenerator;

    private HashMap<String, FieldViewModel> currentFieldViewModels;


    public RulesUtilsProviderImpl(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    @Override
    public void applyRuleEffects(Map<String, FieldViewModel> fieldViewModels,
                                 Result<RuleEffect> calcResult,
                                 @NonNull RulesActionCallbacks rulesActionCallbacks) {

        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();

            if (ruleAction instanceof RuleActionShowWarning)
                showWarning((RuleActionShowWarning) ruleAction, fieldViewModels);
            else if (ruleAction instanceof RuleActionShowError)
                showError((RuleActionShowError) ruleAction, fieldViewModels, rulesActionCallbacks);
            else if (ruleAction instanceof RuleActionHideField)
                hideField((RuleActionHideField) ruleAction, fieldViewModels, rulesActionCallbacks);
            else if (ruleAction instanceof RuleActionDisplayText)
                displayText((RuleActionDisplayText) ruleAction, ruleEffect, fieldViewModels);
            else if (ruleAction instanceof RuleActionDisplayKeyValuePair)
                displayKeyValuePair((RuleActionDisplayKeyValuePair) ruleAction, ruleEffect, rulesActionCallbacks);
            else if (ruleAction instanceof RuleActionHideSection)
                hideSection((RuleActionHideSection) ruleAction, rulesActionCallbacks);
            else if (ruleAction instanceof RuleActionAssign)
                assign((RuleActionAssign) ruleAction, ruleEffect, fieldViewModels, rulesActionCallbacks);
            else if (ruleAction instanceof RuleActionCreateEvent)
                createEvent((RuleActionCreateEvent) ruleAction, fieldViewModels, rulesActionCallbacks);
            else if (ruleAction instanceof RuleActionSetMandatoryField)
                setMandatory((RuleActionSetMandatoryField) ruleAction, fieldViewModels);
            else if (ruleAction instanceof RuleActionWarningOnCompletion)
                warningOnCompletion((RuleActionWarningOnCompletion) ruleAction, rulesActionCallbacks);
            else if (ruleAction instanceof RuleActionErrorOnCompletion)
                errorOnCompletion((RuleActionErrorOnCompletion) ruleAction, rulesActionCallbacks);
            else if (ruleAction instanceof RuleActionHideProgramStage)
                hideProgramStage((RuleActionHideProgramStage) ruleAction, rulesActionCallbacks);
            else
                rulesActionCallbacks.unsupportedRuleAction();

        }

        if (currentFieldViewModels == null)
            currentFieldViewModels = new HashMap<>();
        currentFieldViewModels.clear();
        currentFieldViewModels.putAll(fieldViewModels);
    }

    @Override
    public void applyRuleEffects(Map<String, ProgramStageModel> programStages, Result<RuleEffect> calcResult) {
        for (RuleEffect ruleEffect : calcResult.items()) {
            if (ruleEffect.ruleAction() instanceof RuleActionHideProgramStage)
                hideProgramStage(programStages, (RuleActionHideProgramStage) ruleEffect.ruleAction());
        }
    }


    private void showWarning(RuleActionShowWarning showWarning,
                             Map<String, FieldViewModel> fieldViewModels) {

        FieldViewModel model = fieldViewModels.get(showWarning.field());

        if (model != null)
            fieldViewModels.put(showWarning.field(), model.withWarning(showWarning.content()));
        else
            Timber.d("Field with uid %s is missing", showWarning.field());

    }

    private void showError(RuleActionShowError showError,
                           Map<String, FieldViewModel> fieldViewModels,
                           RulesActionCallbacks rulesActionCallbacks) {
        FieldViewModel model = fieldViewModels.get(showError.field());

        if (model != null)
            fieldViewModels.put(showError.field(), model.withError(showError.content()));
        else
            Timber.d("Field with uid %s is missing", showError.field());

        rulesActionCallbacks.setShowError(showError);
    }

    private void hideField(RuleActionHideField hideField, Map<String,
            FieldViewModel> fieldViewModels,
                           RulesActionCallbacks rulesActionCallbacks) {
        fieldViewModels.remove(hideField.field());
        rulesActionCallbacks.save(hideField.field(), null);
    }

    private void displayText(RuleActionDisplayText displayText,
                             RuleEffect ruleEffect,
                             Map<String, FieldViewModel> fieldViewModels) {
        String uid = displayText.content();

        EditTextViewModel textViewModel = EditTextViewModel.create(uid,
                displayText.content(), false, ruleEffect.data(), "Information", 1, ValueType.TEXT, null, false, null);

        if (this.currentFieldViewModels == null ||
                !this.currentFieldViewModels.containsKey(uid)) {
            fieldViewModels.put(uid, textViewModel);
        } else if (this.currentFieldViewModels.containsKey(uid) &&
                !Objects.equals(currentFieldViewModels.get(uid).value(), textViewModel.value())) {
            fieldViewModels.put(uid, textViewModel);
        }
    }

    private void displayKeyValuePair(RuleActionDisplayKeyValuePair displayKeyValuePair,
                                     RuleEffect ruleEffect,
                                     RulesActionCallbacks rulesActionCallbacks) {
        rulesActionCallbacks.setDisplayKeyValue(displayKeyValuePair.content(), ruleEffect.data());
    }

    private void hideSection(RuleActionHideSection hideSection,
                             RulesActionCallbacks rulesActionCallbacks) {
        rulesActionCallbacks.sethideSection(hideSection.programStageSection());
    }

    private void assign(RuleActionAssign assign, RuleEffect ruleEffect,
                        Map<String, FieldViewModel> fieldViewModels, RulesActionCallbacks rulesActionCallbacks) {

        if (fieldViewModels.get(assign.field()) == null)
            rulesActionCallbacks.save(assign.field(), ruleEffect.data());
        else {
            String value = fieldViewModels.get(assign.field()).value();

            if (value == null || !value.equals(ruleEffect.data())) {
                rulesActionCallbacks.save(assign.field(), ruleEffect.data());
            }

            fieldViewModels.put(assign.field(), fieldViewModels.get(assign.field()).withValue(ruleEffect.data()));

        }
    }

    private void createEvent(RuleActionCreateEvent createEvent, Map<String, FieldViewModel> fieldViewModels, RulesActionCallbacks rulesActionCallbacks) {
        //TODO: Create Event
    }

    private void setMandatory(RuleActionSetMandatoryField mandatoryField, Map<String, FieldViewModel> fieldViewModels) {
        FieldViewModel model = fieldViewModels.get(mandatoryField.field());
        if (model != null)
            fieldViewModels.put(mandatoryField.field(), model.setMandatory());
    }

    private void warningOnCompletion(RuleActionWarningOnCompletion warningOnCompletion, RulesActionCallbacks rulesActionCallbacks) {
        rulesActionCallbacks.setMessageOnComplete(warningOnCompletion.content(), true);
    }

    private void errorOnCompletion(RuleActionErrorOnCompletion errorOnCompletion, RulesActionCallbacks rulesActionCallbacks) {
        rulesActionCallbacks.setMessageOnComplete(errorOnCompletion.content(), false);
    }


    private void hideProgramStage(RuleActionHideProgramStage hideProgramStage, RulesActionCallbacks rulesActionCallbacks) {
        rulesActionCallbacks.setHideProgramStage(hideProgramStage.programStage());
    }

    private void hideProgramStage(Map<String, ProgramStageModel> programStages, RuleActionHideProgramStage hideProgramStage) {
        programStages.remove(hideProgramStage.programStage());
    }
}
