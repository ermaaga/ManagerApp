package it.uniba.di.sms2021.managerapp.lists;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.uniba.di.sms2021.managerapp.R;

public class ProjectsRecyclerViewManager {
    private RecyclerView recyclerView;

    private boolean shareable = true;
    private ImageView shareButton;
    private TextView emptyTextView;
    private ImageView minimizeButton;
    private ProjectsRecyclerAdapter projectsAdapter;
    private ConstraintLayout projectsLayout;

    private boolean expanded = true;

    private ProjectsRecyclerViewManager() {

    }

    public static Builder getBuilder (RecyclerView recyclerView) {
        return new Builder(recyclerView);
    }

    public ImageView getShareButton() {
        return shareButton;
    }

    public boolean isShareable() {
        return shareable;
    }

    /**
     * Nasconde o mostra il bottone di condivisione in base a se sia possibile condividere la lista
     * di progetti o no.
     */
    public void setShareable(boolean shareable) {
        this.shareable = shareable;
        if (shareable) {
            shareButton.setVisibility(View.VISIBLE);
        } else {
            shareButton.setVisibility(View.GONE);
        }
    }

    public void setProjectsViewHasData (boolean hasData) {
        if (hasData) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    private void setProjectsExpanded(boolean expanded) {
        if (expanded) {
            projectsLayout.setVisibility(View.VISIBLE);
        } else {
            projectsLayout.setVisibility(View.GONE);
        }
    }

    public static class Builder {
        private ProjectsRecyclerViewManager projectsRecyclerViewManager;
        private Context context;

        public Builder(RecyclerView recyclerView) {
            this.projectsRecyclerViewManager = new ProjectsRecyclerViewManager();
            this.projectsRecyclerViewManager.recyclerView = recyclerView;
            this.context = recyclerView.getContext();
        }

        public ProjectsRecyclerViewManager build () {
            if (!projectsRecyclerViewManager.shareable) {
                projectsRecyclerViewManager.shareButton.setVisibility(View.GONE);
            }

            projectsRecyclerViewManager.recyclerView.setAdapter(projectsRecyclerViewManager.projectsAdapter);
            projectsRecyclerViewManager.recyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL));
            projectsRecyclerViewManager.recyclerView.setLayoutManager(new LinearLayoutManager(context));

            initialiseExpandButton ();

            return projectsRecyclerViewManager;
        }

        private void initialiseExpandButton() {
            projectsRecyclerViewManager.minimizeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (projectsRecyclerViewManager.expanded) {
                        projectsRecyclerViewManager.expanded = false;
                        projectsRecyclerViewManager.minimizeButton.setImageDrawable(
                                ContextCompat.getDrawable(context, R.drawable.ic_baseline_expand_24));
                        projectsRecyclerViewManager.setProjectsExpanded(false);
                    } else {
                        projectsRecyclerViewManager.expanded = true;
                        projectsRecyclerViewManager.minimizeButton.setImageDrawable(
                                ContextCompat.getDrawable(context, R.drawable.ic_baseline_minimize_24));
                        projectsRecyclerViewManager.setProjectsExpanded(true);
                    }
                }
            });
        }

        public Builder withShareButton (ImageView shareButton) {
            projectsRecyclerViewManager.shareButton = shareButton;
            return this;
        }

        public Builder shareable (boolean shareable) {
            projectsRecyclerViewManager.shareable = shareable;
            return this;
        }

        public Builder withEmptyTextView(TextView emptyTextView) {
            projectsRecyclerViewManager.emptyTextView = emptyTextView;
            return this;
        }

        public Builder withMinimizeButton(ImageView minimizeButton) {
            projectsRecyclerViewManager.minimizeButton = minimizeButton;
            return this;
        }

        public Builder withAdapter(ProjectsRecyclerAdapter projectsAdapter) {
            projectsRecyclerViewManager.projectsAdapter = projectsAdapter;
            projectsAdapter.setRecyclerViewManager(projectsRecyclerViewManager);

            return this;
        }

        public Builder withProjectsLayout (ConstraintLayout projectsLayout) {
            projectsRecyclerViewManager.projectsLayout = projectsLayout;
            return this;
        }
    }
}
