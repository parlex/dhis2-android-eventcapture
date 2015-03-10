/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis2.android.eventcapture.fragments;

import android.app.Fragment;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.controllers.datavalues.DataValueController;
import org.hisp.dhis2.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis2.android.sdk.utils.ui.rows.AutoCompleteRow;
import org.hisp.dhis2.android.sdk.utils.ui.rows.BooleanRow;
import org.hisp.dhis2.android.sdk.utils.ui.rows.CheckBoxRow;
import org.hisp.dhis2.android.sdk.utils.ui.rows.DatePickerRow;
import org.hisp.dhis2.android.sdk.utils.ui.rows.IntegerRow;
import org.hisp.dhis2.android.sdk.utils.ui.rows.LongTextRow;
import org.hisp.dhis2.android.sdk.utils.ui.rows.NegativeIntegerRow;
import org.hisp.dhis2.android.sdk.utils.ui.rows.NumberRow;
import org.hisp.dhis2.android.sdk.utils.ui.rows.PosIntegerRow;
import org.hisp.dhis2.android.sdk.utils.ui.rows.PosOrZeroIntegerRow;
import org.hisp.dhis2.android.sdk.utils.ui.rows.Row;
import org.hisp.dhis2.android.sdk.utils.ui.rows.TextRow;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.events.BaseEvent;
import org.hisp.dhis2.android.sdk.events.MessageEvent;
import org.hisp.dhis2.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis2.android.sdk.persistence.models.DataElement;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue;
import org.hisp.dhis2.android.sdk.persistence.models.Event;
import org.hisp.dhis2.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis2.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis2.android.sdk.persistence.models.Program;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis2.android.sdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Simen Skogly Russnes on 20.02.15.
 */
public class DataEntryFragment extends Fragment {

    private static final String CLASS_TAG = "DataEntryFragment";

    private OrganisationUnit selectedOrganisationUnit;
    private Program selectedProgram;
    private ProgramStage selectedProgramStage;

    private TextView organisationUnitLabel;
    private TextView programLabel;
    private Button captureCoordinateButton;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private Event event;
    private String editingEvent;
    private List<DataValue> dataValues;
    private List<ProgramStageDataElement> programStageDataElements;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_register_event,
                container, false);
        setupUi(rootView);
        return rootView;
    }

    public void setupUi(View rootView) {
        organisationUnitLabel = (TextView) rootView.findViewById(R.id.dataentry_orgunitlabel);
        programLabel = (TextView) rootView.findViewById(R.id.dataentry_programlabel);
        captureCoordinateButton = (Button) rootView.findViewById(R.id.dataentry_getcoordinatesbutton);
        latitudeEditText = (EditText) rootView.findViewById(R.id.dataentry_latitudeedit);
        longitudeEditText = (EditText) rootView.findViewById(R.id.dataentry_longitudeedit);

        if(selectedOrganisationUnit == null || selectedProgram == null) return;

        organisationUnitLabel.setText(selectedOrganisationUnit.getLabel());
        programLabel.setText(selectedProgram.getName());

        LinearLayout dataElementContainer = (LinearLayout) rootView.
                findViewById(R.id.dataentry_dataElementContainer);
        setupDataEntryForm(dataElementContainer);
    }

    public void setupDataEntryForm(LinearLayout dataElementContainer) {
        selectedProgramStage = selectedProgram.getProgramStages().get(0); //since this is event capture, there will only be 1 stage.
        programStageDataElements = selectedProgramStage.getProgramStageDataElements();

        if(editingEvent == null) {
            createNewEvent();
        } else {
            loadEvent();
        }

        if(!selectedProgramStage.captureCoordinates) {
            disableCaptureCoordinates();
        } else {
            Dhis2.activateGps(getActivity());
            captureCoordinateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getCoordinates();
                }
            });
        }

        for(int i = 0; i<programStageDataElements.size(); i++) {
            View view = createDataEntryView(programStageDataElements.get(i),
                    getDataValue(programStageDataElements.get(i).dataElement, dataValues));
            CardView cardView = new CardView(getActivity());

            Resources r = getActivity().getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    6,
                    r.getDisplayMetrics());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(px, px, px, 0);
            cardView.setLayoutParams(params);
            cardView.addView(view);
            dataElementContainer.addView(cardView);
        }
    }

    /**
     * Returns the DataValue associated with the given programStageDataElement from a list of DataValues
     * @param dataValues
     * @return
     */
    public DataValue getDataValue(String dataElement, List<DataValue> dataValues) {
        for(DataValue dataValue: dataValues) {
            if(dataValue.dataElement.equals(dataElement))
                return dataValue;
        }

        //the datavalue didnt exist for some reason. Create a new one.
        DataValue dataValue = new DataValue(event.event, "",
                dataElement, false,
                Dhis2.getInstance().getUsername(getActivity()));
        dataValues.add(dataValue);
        return dataValue;
    }

    public void loadEvent() {
        event = DataValueController.getEvent(editingEvent);
        dataValues = event.getDataValues();
    }

    public void createNewEvent() {
        event = new Event();
        event.event = Dhis2.QUEUED + UUID.randomUUID().toString();
        event.fromServer = false;
        event.dueDate = Utils.getCurrentDate();
        event.eventDate = Utils.getCurrentDate();
        event.organisationUnitId = selectedOrganisationUnit.getId();
        event.programId = selectedProgram.id;
        event.programStageId = selectedProgram.getProgramStages().get(0).id;
        event.status = Event.STATUS_COMPLETED;
        dataValues = new ArrayList<DataValue>();
        for(int i = 0; i<programStageDataElements.size(); i++) {
            ProgramStageDataElement programStageDataElement = programStageDataElements.get(i);
            dataValues.add(new DataValue(event.event, "",
                    programStageDataElement.dataElement, false,
                    Dhis2.getUsername(getActivity())));
        }
    }

    /**
     * Gets coordinates from the device GPS if possible and stores in the current Event.
     */
    public void getCoordinates() {
        Location location = Dhis2.getLocation(getActivity());
        event.latitude = location.getLatitude();
        event.longitude = location.getLongitude();
        latitudeEditText.setText(""+event.latitude);
        longitudeEditText.setText(""+event.longitude);
    }

    public void disableCaptureCoordinates() {
        longitudeEditText.setVisibility(View.GONE);
        latitudeEditText.setVisibility(View.GONE);
        captureCoordinateButton.setVisibility(View.GONE);
    }

    public View createDataEntryView(ProgramStageDataElement programStageDataElement, DataValue dataValue) {
        DataElement dataElement = MetaDataController.getDataElement(programStageDataElement.dataElement);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Row row = null;
        if (dataElement.getOptionSet() != null) {
            OptionSet optionSet = MetaDataController.getOptionSet(dataElement.optionSet);
            if(optionSet == null)
                row = new TextRow(inflater, programStageDataElement, dataValue);
            else
                row = new AutoCompleteRow(inflater, programStageDataElement, optionSet, dataValue, getActivity());
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_TEXT)) {
            row = new TextRow(inflater, programStageDataElement, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_LONG_TEXT)) {
            row = new LongTextRow(inflater, programStageDataElement, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_NUMBER)) {
            row = new NumberRow(inflater, programStageDataElement, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_INT)) {
            row = new IntegerRow(inflater, programStageDataElement, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_ZERO_OR_POSITIVE_INT)) {
            row = new PosOrZeroIntegerRow(inflater, programStageDataElement, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_POSITIVE_INT)) {
            row = new PosIntegerRow(inflater, programStageDataElement, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_NEGATIVE_INT)) {
            row = new NegativeIntegerRow(inflater, programStageDataElement, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_BOOL)) {
            row = new BooleanRow(inflater, programStageDataElement, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_TRUE_ONLY)) {
            row = new CheckBoxRow(inflater, programStageDataElement, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_DATE)) {
            row = new DatePickerRow(inflater, programStageDataElement, getActivity(), dataValue);
        } else {
            Log.d(CLASS_TAG, "type is: " + dataElement.getType());
        }
        if( row==null) return new View(getActivity());
        return row.getView(null);
    }

    /**
     * saves the current data values as a registered event.
     */
    public void submit() {
        boolean valid = true;
        //go through each data element and check that they are valid
        //i.e. all compulsory are not empty
        for(int i = 0; i<dataValues.size(); i++) {
            ProgramStageDataElement programStageDataElement = programStageDataElements.get(i);
            if( programStageDataElement.isCompulsory() ) {
                DataValue dataValue = dataValues.get(i);
                if(dataValue.value == null || dataValue.value.length() <= 0) {
                    valid = false;
                }
            }
        }

        if(!valid) {
            Dhis2.getInstance().showErrorDialog(getActivity(), "Validation error",
                    "Some compulsory fields are empty, please fill them in");
        } else {
            saveEvent();
            showSelectProgramFragment();
        }
    }

    public void saveEvent() {
        event.fromServer = false;
        event.save(false);
        for(DataValue dataValue: dataValues) {
            dataValue.save(false);
        }
    }

    public void showSelectProgramFragment() {
        MessageEvent event = new MessageEvent(BaseEvent.EventType.showSelectProgramFragment);
        Dhis2Application.bus.post(event);
    }

    public OrganisationUnit getSelectedOrganisationUnit() {
        return selectedOrganisationUnit;
    }

    public void setSelectedOrganisationUnit(OrganisationUnit selectedOrganisationUnit) {
        this.selectedOrganisationUnit = selectedOrganisationUnit;
    }

    public Program getSelectedProgram() {
        return selectedProgram;
    }

    public void setSelectedProgram(Program selectedProgram) {
        this.selectedProgram = selectedProgram;
    }

    public void setEditingEvent(String event) {
        this.editingEvent = event;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Dhis2.disableGps();
    }
}
