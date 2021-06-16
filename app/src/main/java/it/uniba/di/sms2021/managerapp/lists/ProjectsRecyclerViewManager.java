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
        private ProjectsRecyclerViewManager projectsRecyclerView;
        private Context context;

        public Builder(RecyclerView recyclerView) {
            this.projectsRecyclerView = new ProjectsRecyclerViewManager();
            this.projectsRecyclerView.recyclerView = recyclerView;
            this.context = recyclerView.getContext();
        }

        public ProjectsRecyclerViewManager build () {
            if (!projectsRecyclerView.shareable) {
                projectsRecyclerView.shareButton.setVisibility(View.GONE);
            }

            projectsRecyclerView.recyclerView.setAdapter(projectsRecyclerView.projectsAdapter);
            projectsRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL));
            projectsRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(context));

            initialiseExpandButton ();

            return projectsRecyclerView;
        }

        private void initialiseExpandButton() {
            projectsRecyclerView.minimizeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (projectsRecyclerView.expanded) {
                        projectsRecyclerView.expanded = false;
                        projectsRecyclerView.minimizeButton.setImageDrawable(
                                ContextCompat.getDrawable(context, R.drawable.ic_baseline_expand_24));
                        projectsRecyclerView.setProjectsExpanded(false);
                    } else {
                        projectsRecyclerView.expanded = true;
                        projectsRecyclerView.minimizeButton.setImageDrawable(
                                ContextCompat.getDrawable(context, R.drawable.ic_baseline_minimize_24));
                        projectsRecyclerView.setProjectsExpanded(true);
                    }
                }
            });
        }

        public Builder withShareButton (ImageView shareButton) {
            projectsRecyclerView.shareButton = shareButton;
            return this;
        }

        public Builder shareable (boolean shareable) {
            projectsRecyclerView.shareable = shareable;
            return this;
        }

        public Builder withEmptyTextView(TextView emptyTextView) {
            projectsRecyclerView.emptyTextView = emptyTextView;
            return this;
        }

        public Builder withMinimizeButton(ImageView minimizeButton) {
            projectsRecyclerView.minimizeButton = minimizeButton;
            return this;
        }

        public Builder withAdapter(ProjectsRecyclerAdapter projectsAdapter) {
            projectsRecyclerView.projectsAdapter = projectsAdapter;
            return this;
        }

        public Builder withProjectsLayout (ConstraintLayout projectsLayout) {
            projectsRecyclerView.projectsLayout = projectsLayout;
            return this;
        }
    }
}
