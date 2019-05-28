package com.talismansoftwaresolutions.talismantasklist;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {
    private ArrayList<TaskCLS> taskList;
    private Context ctx;

    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvTask;
        TextView tvViewOption;
        int taskPosition;
        final TaskListAdapter adapter;

        public TaskViewHolder(View viewItem, TaskListAdapter adapter) {
            super(viewItem);

            this.adapter = adapter;
            tvTask = itemView.findViewById(R.id.task_name);
            tvViewOption = itemView.findViewById(R.id.textViewOptions);

            viewItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            TaskViewHolder holder = this;

            taskPosition = getLayoutPosition();  //getAdapterPosition
            TaskCLS task = taskList.get(taskPosition);

            showTaskDetails(this, v, taskPosition);

            //adapter.notifyDataSetChanged();

        }// end onClick


        private void showTaskDetails(RecyclerView.ViewHolder viewHolder, View v, int nPosition) {

            TaskCLS task = taskList.get(nPosition);

            DataHolder dataHolder = DataHolder.getInstance();

            //dataHolder.clear();
            dataHolder.save("Task", task);
            dataHolder.save("ViewHolder", viewHolder);


            Intent intent = new Intent(v.getContext(), TaskDetailsActivity.class);
            v.getContext().startActivity(intent);

        }


        public void itemRemoved() {
            DataHolder dataHolder = DataHolder.getInstance();
            TaskCLS task = (TaskCLS)dataHolder.retrieve("Task");
            int id = task.getTaskID();
            String taskName = task.getTaskName();

            if(task != null) {

                try {
                    (DatabaseHelper.getInstance(ctx)).deleteTask(task.getTaskID());
                } catch (Exception ex) {
                    Util.writeToLog("TaskViewHolder.itemRemoved: Could not remove task from database (" + String.valueOf(id) + ")");
                    return;
                }

                taskList.remove(taskPosition);
                adapter.notifyItemRemoved(taskPosition);


                Util.makeToast(ctx, "Task deleted: " + taskName);
            }
        }

        public void dataChanged(boolean taskDetailsChanged) {
            if(taskDetailsChanged) {
                DataHolder dataHolder = DataHolder.getInstance();
                TaskCLS task = (TaskCLS)dataHolder.retrieve("Task");
                String taskName = task.getTaskName();
                int taskID = task.getTaskID();

                if(task != null) {

                    try {
                        (DatabaseHelper.getInstance(ctx)).updateTask(task.getTaskID(), taskName);
                    } catch (Exception ex) {
                        Util.writeToLog("datachanged: Could not remove task from database (" + String.valueOf(taskID) + ")");
                        return;
                    }

                    taskList.set(taskPosition, task);
                    adapter.notifyItemChanged(taskPosition);


                    Util.makeToast(ctx, "Edited task");
                }
            }
        }

    }// end viewholder

    public TaskListAdapter(Context context, ArrayList<TaskCLS> taskList) {
        this.taskList = taskList;
        this.ctx = context;
    }

    @Override
    public TaskListAdapter.TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tasklist_item,
                parent, false);
        return(new TaskViewHolder(v, this));
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, final int position) {
        final TaskCLS task = taskList.get(position);
        final int taskShowingMode = ((MainActivity)ctx).getTasksShowingMode();


        holder.tvTask.setText(task.getTaskName());

        holder.tvViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(ctx, holder.tvViewOption);
                switch(taskShowingMode) {
                    case Constants.ARCHIVED_TASKS:
                        popup.inflate(R.menu.menu_unarchive);
                        break;

                    case Constants.DELETED_TASKS:
                        popup.inflate(R.menu.menu_undelete);
                        break;

                    default:
                        popup.inflate(R.menu.options_menu);
                        break;
                }

                //popup.inflate(R.menu.options_menu);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_item_archive:
                                //Util.makeToast(ctx, "Task " + task.getTaskName() + " will be archived");
                                ((MainActivity)ctx).setTaskToArchive(task); //position
                                break;

                            case R.id.menu_item_unarchive:
                                //Util.makeToast(ctx, "Task " + task.getTaskName() + " will be archived");
                                ((MainActivity)ctx).unArchiveTask(task); //position
                                break;

                            case R.id.menu_item_undelete:
                                //Util.makeToast(ctx, "Undeleting " + task.getTaskName());
                                ((MainActivity)ctx).unDeleteTask(task);
                                break;

                            case R.id.menu_item_delete:
                                //Util.makeToast(ctx, "Undeleting " + task.getTaskName());
                                ((MainActivity)ctx).deleteTask(task);
                                break;

                            default:
                                break;
                        }

                        return false;
                    }
                });

                popup.show();

            }// end onClick
        });

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}// end class