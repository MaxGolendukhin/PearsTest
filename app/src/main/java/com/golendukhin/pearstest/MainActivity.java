package com.golendukhin.pearstest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final int[] questionType =
            {0, 0, 1, 1, 0, 0, 0, 0, 0, 2};//list if question 0 - radioButton, 1 - checkBoxes, 2 - editText
    private final int UNSELECTED = 0;
    private final int SELECTED = 1;
    private final int CORRECT = 2;
    Resources resources;
    TextView questionTextView;
    Button previousButton;
    Button finishButton;
    Button nextButton;
    private int questionNumber; //tracks current question number
    private int[][] selectedOptions; //how user has already answered questions
    private int[][] answers; //correct answers
    private String[] questions; //list of all questions to display
    private String[][] options; //list of all options in all questions to display
    private LinearLayout optionsLinearLayout;
    private LinearLayout.LayoutParams linearLayoutParams;
    private String textForEditText = "";

    /**
     * Creates MainActivity
     * Initializes all variables and updates linear layout with question options
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resources = getResources();
        questions = resources.getStringArray(R.array.questions);
        options = getOptions();
        answers = getAnswers();
        selectedOptions = createEmptyLisForSelectedOptions(answers);

        optionsLinearLayout = findViewById(R.id.options_linear_layout);
        linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParams.setMargins(0, 0, 0,
                (int) resources.getDimension(R.dimen.margin_bottom));
        questionTextView = findViewById(R.id.question_text_text_view);
        previousButton = findViewById(R.id.previous_button);
        finishButton = findViewById(R.id.finish_button);
        nextButton = findViewById(R.id.next_button);

        questionNumber = 0;
        updateOptionsLinearLayout(true);
    }

    /**
     * Saves phone parameters on rotate
     * @param outState used to save question number and options already selected
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("questionNumber", questionNumber);
        outState.putSerializable("selectedOptions", selectedOptions);
        outState.putString("textForEditText", textForEditText);
        super.onSaveInstanceState(outState);
    }

    /**
     * Restores MainActivity after phone was rotated
     * Updates linear layout with question options
     * @param savedInstanceState used to restore question number and selected options
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        questionNumber = savedInstanceState.getInt("questionNumber");
        selectedOptions = (int[][]) savedInstanceState.getSerializable("selectedOptions");
        textForEditText = savedInstanceState.getString("textForEditText");
        previousButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        updateOptionsLinearLayout(true);
        updateFinishButton();
    }

    /**
     * Updates linear layout with options for current question, also updates buttons visibility
     * Sets animations, first swaps children views, then replace linear layout and then animate appearance of updated layout
     * @param isNext if need to animate next option
     */
    private void updateOptionsLinearLayout(boolean isNext) {
        Animation animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        animationFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                questionTextView.setText(questions[questionNumber]);
                Animation animationFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                questionTextView.startAnimation(animationFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        questionTextView.startAnimation(animationFadeOut);

        Animation animationOut;
        final Animation animationIn;

        if (isNext) {
            animationOut = AnimationUtils.loadAnimation(this, R.anim.translate_left_off);
            animationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_from_right);
        } else {
            animationOut = AnimationUtils.loadAnimation(this, R.anim.translate_right_off);
            animationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_from_left);
        }

        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                optionsLinearLayout.removeAllViews();

                if (questionType[questionNumber] == 0) {
                    addRadioGroup(options[questionNumber]);
                } else if (questionType[questionNumber] == 1) {
                    addCheckboxes(options[questionNumber]);
                } else {
                    addEditTextView();
                }
                optionsLinearLayout.startAnimation(animationIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        optionsLinearLayout.startAnimation(animationOut);

        updateButtonsVisibility();
    }

    /**
     * Adds editText widget to linear layout
     */
    private void addEditTextView() {
        final EditText editText = new EditText(this);
        editText.setText(textForEditText);
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
                String editedText = editable.toString().toLowerCase();
                if (editedText.equals("")) {
                    selectedOptions[questionNumber][0] = UNSELECTED;
                } else {
                    selectedOptions[questionNumber][0] = SELECTED;
                }

                if (editedText.equals(resources.getString(R.string.lastQuestionAnswer).toLowerCase())) {
                    selectedOptions[questionNumber][0] = CORRECT;
                }
                textForEditText = editedText;

                updateFinishButton();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        optionsLinearLayout.addView(editText);
    }

    /**
     * Adds radio group to linear layout if question implies single question
     * @param options for current question to display
     */
    private void addRadioGroup(String[] options) {
        RadioGroup radioGroup = createRadioGroup(options);

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
            radioButton.setLayoutParams(linearLayoutParams);
            radioButton.setTextSize(resources.getDimension(R.dimen.options_text_size));
            radioButton.setChecked(selectedOptions[questionNumber][i] == 1);//set radioButton checked, if option already selected
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                //since radio button implies only one option, need set list to zeroes while selecting option
                for (int j = 0; j < radioGroup.getChildCount(); j++) {
                    selectedOptions[questionNumber][j] = UNSELECTED;
                }
                int selectedOptionId = radioGroup.getCheckedRadioButtonId();
                selectedOptions[questionNumber][selectedOptionId] = SELECTED;

                if (areAllOptionsSelected()) {
                    finishButton.setVisibility(View.VISIBLE);
                }
            }
        });
        optionsLinearLayout.addView(radioGroup);
    }

    /**
     * Adds check boxes to linear layout if question implies multiple question
     * @param options for current question to display
     */
    private void addCheckboxes(String[] options) {
        int checkBoxesQuantity = options.length;

        for (int i = 0; i < checkBoxesQuantity; i++) {
            final CheckBox checkBox = new CheckBox(this);
            checkBox.setText(options[i]);
            checkBox.setId(i);
            checkBox.setTextSize(resources.getDimension(R.dimen.options_text_size));
            checkBox.setLayoutParams(linearLayoutParams);
            checkBox.setChecked(selectedOptions[questionNumber][i] == 1);//set checkBox checked, if option already selected

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int checkedBoxId = checkBox.getId();
                    selectedOptions[questionNumber][checkedBoxId] = b ? SELECTED : UNSELECTED;
                    updateFinishButton();
                }
            });
            optionsLinearLayout.addView(checkBox);
        }
    }

    /**
     * Every time optionsLinearLayout is updated need also to update buttons visibility
     */
    private void updateButtonsVisibility() {
        if (questionNumber == 0) {
            previousButton.setVisibility(View.INVISIBLE);
        } else if (questionNumber == questions.length - 1) {
            nextButton.setVisibility(View.INVISIBLE);
        } else {
            previousButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * If all questions are answered, need to show finish button
     */
    private void updateFinishButton() {
        if (areAllOptionsSelected()) {
            finishButton.setVisibility(View.VISIBLE);
        } else {
            finishButton.setVisibility(View.GONE);
        }
    }

    /**
     * Every time any option is selected need to define are all questions answered
     * Used to show finish button
     * @return true if all question answered, false otherwise
     */
    private boolean areAllOptionsSelected() {
        int allOptionsSelected = 0;
        int questionsQuantity = selectedOptions.length;
        for (int[] options : selectedOptions) {
            for (int option : options) {
                if (option == SELECTED || option == CORRECT) {
                    allOptionsSelected++;
                    break;
                }
            }
        }
        return allOptionsSelected == questionsQuantity;
    }

    /**
     * Initializes radio group to add to linear layout
     * @param options for current question
     * @return created radio group
     */
    private RadioGroup createRadioGroup(String[] options) {
        int radioButtonsQuantity = options.length;
        final RadioButton[] radioButtons = new RadioButton[radioButtonsQuantity];
        RadioGroup radioGroup = new RadioGroup(this);

        for (int i = 0; i < radioButtonsQuantity; i++) {
            radioButtons[i] = new RadioButton(this);
            radioButtons[i].setText(options[i]);
            radioButtons[i].setId(i);
            radioGroup.addView(radioButtons[i]);
        }
        return radioGroup;
    }

    /**
     * Creates empty list for further answers
     * @param answers list with correct answers for every question
     * @return created empty 2d list
     */
    private int[][] createEmptyLisForSelectedOptions(int[][] answers) {
        int length = answers.length;
        int[][] selectedOptions = new int[length][];
        for (int i = 0; i < length; i++) {
            int[] subArray = new int[answers[i].length];
            selectedOptions[i] = subArray;
        }
        return selectedOptions;
    }

    /**
     * Create list with options to display within every question via xml resources
     * @return list with options
     */
    private String[][] getOptions() {
        Resources resources = getResources();
        TypedArray typedArray = resources.obtainTypedArray(R.array.options);
        int length = typedArray.length();

        String[][] options = new String[length][];
        for (int i = 0; i < length; ++i) {
            int id = typedArray.getResourceId(i, 0);
            options[i] = resources.getStringArray(id);
        }
        typedArray.recycle();

        return options;
    }

    /**
     * Create list with correct answers for every question via xml resources
     * @return list with answers
     */
    private int[][] getAnswers() {
        Resources resources = getResources();
        TypedArray typedArray = resources.obtainTypedArray(R.array.answers);
        int length = typedArray.length();

        int[][] answers = new int[length][];
        for (int i = 0; i < length; ++i) {
            int id = typedArray.getResourceId(i, 0);
            answers[i] = resources.getIntArray(id);
        }
        typedArray.recycle();

        return answers;
    }

    /**
     * Next button pressed, increment number of question and update linear layout
     */
    public void nextQuestion(View view) {
        if (questionNumber < questions.length - 1) {
            questionNumber++;
        }
        updateOptionsLinearLayout(true);
        Toast.makeText(getApplicationContext(),
                resources.getString(R.string.results_dialog,
                        String.valueOf(calculateResult()),
                        String.valueOf(questions.length)),
                Toast.LENGTH_SHORT).show();

    }

    /**
     * Previous button pressed, increment number of question and update linear layout
     */
    public void previousQuestion(View view) {
        if (questionNumber > 0) {
            questionNumber--;
        }
        updateOptionsLinearLayout(false);
        Toast.makeText(getApplicationContext(),
                resources.getString(R.string.results_dialog,
                        String.valueOf(calculateResult()),
                        String.valueOf(questions.length)),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Finish button pressed
     * Initializes alert dialog with thee options: show correct answers, cancel dialog and reset test option
     */
    public void finish(View view) {
        new AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.results_dialog,
                        String.valueOf(calculateResult()), String.valueOf(questions.length)))
                .setPositiveButton(getString(R.string.show_correct_answers_dialog_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedOptions = getAnswers();
                                textForEditText = resources.getString(R.string.lastQuestionAnswer);
                                updateOptionsLinearLayout(true);
                                updateFinishButton();
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(getString(R.string.reset_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedOptions = createEmptyLisForSelectedOptions(answers);
                        questionNumber = 0;
                        updateOptionsLinearLayout(true);
                        finishButton.setVisibility(View.GONE);
                        previousButton.setVisibility(View.INVISIBLE);
                        nextButton.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    /**
     * Triggered if finish button pressed to display progress via alert dialog
     * @return calculated amount of correct answers
     */
    private int calculateResult() {
        int answeredQuestions = 0;
        for (int i = 0; i < selectedOptions.length; i++) {
            boolean questionAnswered = true;
            for (int j = 0; j < selectedOptions[i].length; j++) {
                if (selectedOptions[i][j] != answers[i][j]) {
                    questionAnswered = false;
                    break;
                }
            }
            if (questionAnswered) {
                answeredQuestions++;
            }
        }
        return answeredQuestions;
    }
}