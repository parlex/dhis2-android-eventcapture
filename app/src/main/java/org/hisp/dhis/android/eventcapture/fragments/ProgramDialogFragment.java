/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;

import com.raizlabs.android.dbflow.structure.Model;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.sdk.controllers.Dhis2;
import org.hisp.dhis.android.sdk.persistence.loaders.DbLoader;
import org.hisp.dhis.android.sdk.persistence.loaders.Query;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit$Table;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.utils.ui.dialogs.AutoCompleteDialogAdapter.OptionAdapterValue;
import org.hisp.dhis.android.sdk.utils.ui.dialogs.AutoCompleteDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ProgramDialogFragment extends AutoCompleteDialogFragment
        implements LoaderManager.LoaderCallbacks<List<OptionAdapterValue>> {
    public static final int ID = 921345;
    private static final int LOADER_ID = 1;

    public static ProgramDialogFragment newInstance(OnOptionSelectedListener listener,
                                                     String orgUnitId) {
        ProgramDialogFragment fragment = new ProgramDialogFragment();
        Bundle args = new Bundle();
        args.putString(OrganisationUnit$Table.ID, orgUnitId);
        fragment.setArguments(args);
        fragment.setOnOptionSetListener(listener);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDialogLabel(R.string.dialog_programs);
        setDialogId(ID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, getArguments(), this);
    }

    @Override
    public Loader<List<OptionAdapterValue>> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID == id && isAdded()) {
            String organisationUnitId = args.getString(OrganisationUnit$Table.ID);
            List<Class<? extends Model>> modelsToTrack = new ArrayList<>();
            modelsToTrack.add(Program.class);
            return new DbLoader<>(
                    getActivity().getBaseContext(), modelsToTrack, new ProgramQuery(organisationUnitId)
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<OptionAdapterValue>> loader,
                               List<OptionAdapterValue> data) {
        if (LOADER_ID == loader.getId()) {
            getAdapter().swapData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<OptionAdapterValue>> loader) {
        getAdapter().swapData(null);
    }

    static class ProgramQuery implements Query<List<OptionAdapterValue>> {
        private final String mOrgUnitId;

        public ProgramQuery(String orgUnitId) {
            mOrgUnitId = orgUnitId;
        }

        @Override
        public List<OptionAdapterValue> query(Context context) {
            List<Program> programs = Dhis2.getInstance()
                    .getMetaDataController()
                    .getProgramsForOrganisationUnit(
                            mOrgUnitId, Program.SINGLE_EVENT_WITHOUT_REGISTRATION
                    );
            List<OptionAdapterValue> values = new ArrayList<>();
            if (programs != null && !programs.isEmpty()) {
                for (Program program : programs) {
                    values.add(new OptionAdapterValue(program.getId(), program.getName()));
                }
            }

            return values;
        }
    }
}
