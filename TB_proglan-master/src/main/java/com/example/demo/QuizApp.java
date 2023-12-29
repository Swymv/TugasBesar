package com.example.demo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class QuizApp extends Application {

        Scene scene1, scene2, scene3, scene4, scene5;

        private TextField addQuestion;
        private TextField addAnswer;
        private TextField addPoint;

        private TextField playerName;
        private int currentQuestionIndex = 0;
        private int playerScore = 0;

        private Label playerNameLabel;
        private Label currentQuestionLabel;
        private Stage primaryStage;
        private TextField answerField;

        private List<Quiz> userAnswers = new ArrayList<>();

        public static class Quiz {
            private final SimpleStringProperty question;
            private final SimpleStringProperty answer;
            private final SimpleStringProperty point;
            private final SimpleStringProperty userAnswer;

            public Quiz(String question, String answer, String point, String userAnswer) {
                this.question = new SimpleStringProperty(question);
                this.answer = new SimpleStringProperty(answer);
                this.point = new SimpleStringProperty(point);
                this.userAnswer = new SimpleStringProperty(userAnswer);
            }

            public String getQuestion() {
                return question.get();
            }

            public String getAnswer() {
                return answer.get();
            }

            public String getPoint() {
                return point.get();
            }

            public String getUserAnswer() {
                return userAnswer.get();
            }
        }

        private TableView<Quiz> table = new TableView<>();
        private ObservableList<Quiz> data = FXCollections.observableArrayList();

        private TableView<Quiz> summaryTable = new TableView<>();
        private ObservableList<Quiz> summaryData = FXCollections.observableArrayList();

        private File dataFile = new File("quiz_data.txt");
        private File scoreFile = new File("score.txt");


        public static void main(String[] args) {
            launch(args);
        }

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;

            // Membaca data dari file teks saat aplikasi dimulai
            readDataFromFile();

            GridPane gridPane = new GridPane();
            primaryStage.setTitle("Aplikasi Kuis");


            // Scene 1 Pertanyaan Kuis
            final Label label = new Label("Form Kuis");
            label.setFont(new Font("Arial", 30));


            addQuestion = new TextField();
            addQuestion.setPromptText("(max 100 karakter)");
            addQuestion.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 100) {
                    addQuestion.setText(oldValue);
                    showAlert("Peringatan", "Pertanyaan tidak boleh lebih dari 100 karakter.");
                }
            });


            addAnswer = new TextField();
            addAnswer.setPromptText("(max 50 karakter)");
            addAnswer.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 50) {
                    addAnswer.setText(oldValue);
                    showAlert("Peringatan", "Jawaban tidak boleh lebih dari 50 karakter.");
                }
            });

            addPoint = new TextField();
            addPoint.setPromptText("(max 2 digit)");
            addPoint.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 2) {
                    addPoint.setText(oldValue);
                    showAlert("Peringatan", "Point tidak boleh lebih dari 2 digit.");
                } else if (!newValue.matches("\\d*")) {
                    addPoint.setText(newValue.replaceAll("[^\\d]", ""));
                    showAlert("Peringatan", "Point harus berupa angka.");
                } else if (newValue.equals("0")) { // Penambahan validasi untuk memastikan tidak ada input 0 point
                    addPoint.setText(oldValue);
                    showAlert("Peringatan", "Point tidak boleh 0.");
                }
            });

            Button addButton = new Button("Tambah Kuis");
            addButton.setOnAction(e -> {
                if (isInputValid(addQuestion, addAnswer, addPoint)) {
                    data.add(new Quiz(addQuestion.getText(), addAnswer.getText(), addPoint.getText(), ""));

                    // Menyimpan data ke file teks setiap kali ada perubahan
                    saveDataToFile();

                    addQuestion.clear();
                    addAnswer.clear();
                    addPoint.clear();

                    primaryStage.setScene(scene2);
                } else {
                    showAlert("Peringatan", "Semua kolom harus diisi!");
                }
            });

            //Tombol import kuis
            Button importButton = new Button("Import Kuis");
            importButton.setOnAction(e -> importQuizData());

            gridPane.setVgap(10);
            gridPane.setHgap(10);
            gridPane.setAlignment(Pos.CENTER);

            gridPane.add(label, 0, 0, 2, 1);

            gridPane.add(new Label("Pertanyaan:"), 0, 1);
            gridPane.add(addQuestion, 1, 1);

            gridPane.add(new Label("Jawaban:"), 0, 2);
            gridPane.add(addAnswer, 1, 2);

            gridPane.add(new Label("Point:"), 0, 3);
            gridPane.add(addPoint, 1, 3);

            gridPane.add(addButton, 1, 4);

            scene1 = new Scene(gridPane, 350, 350);

            // Scene 2 Daftar Kuis
            final Label label2 = new Label("Daftar Kuis");
            label2.setFont(new Font("Arial", 30));

            TableColumn<Quiz, String> questionCol = createEditableColumn("Pertanyaan", "question");
            questionCol.setMinWidth(200);
            TableColumn<Quiz, String> answerCol = createEditableColumn("Jawaban", "answer");
            answerCol.setMinWidth(200);
            TableColumn<Quiz, String> pointCol = createEditableColumn("Point", "point");
            pointCol.setMinWidth(50);

            table.getColumns().addAll(questionCol, answerCol, pointCol);


            Button playButton = new Button("Mulai Kuis");
            playButton.setOnAction(e -> {
                playerName = new TextField();
                playerName.setPromptText("(max 20 karakter)");
                playerName.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.length() > 20) {
                        playerName.setText(oldValue);
                        showAlert("Peringatan", "Nama Anda tidak boleh lebih dari 20 karakter.");
                    }
                });

                Label nameLabel = new Label("Masukkan Nama:");
                VBox playVBox = new VBox(10, nameLabel, playerName);
                playVBox.setAlignment(Pos.CENTER);

                Button startQuizButton = new Button("Mulai Kuis");
                startQuizButton.setOnAction(event -> startQuiz());

                VBox playButtonVBox = new VBox(10, playVBox, startQuizButton);
                playButtonVBox.setAlignment(Pos.CENTER);

                scene3 = new Scene(playButtonVBox, 300, 200);
                primaryStage.setScene(scene3);
            });

            Button deleteButton = new Button("Hapus Kuis");
            deleteButton.setOnAction(e -> deleteSelectedQuiz());

            HBox hboxTable = new HBox(10, table);
            hboxTable.setAlignment(Pos.CENTER);

            HBox hboxButtons = new HBox(10, playButton, deleteButton);
            hboxButtons.setAlignment(Pos.CENTER);

            table.setItems(data);

            Button addData = new Button("Tambah Kuis");
            addData.setOnAction(e -> primaryStage.setScene(scene1));

            final VBox vbox = new VBox(10, label2, hboxTable, hboxButtons, addData, importButton);
            vbox.setPadding(new Insets(20));
            vbox.setAlignment(Pos.CENTER);

            scene2 = new Scene(vbox, 600, 400);

            // Scene 4 (Quiz Play Scene) memulai kuis dan menampilkan pertanyaan
            playerNameLabel = new Label();
            playerNameLabel.setFont(new Font("Arial", 20));

            currentQuestionLabel = new Label();

            answerField = new TextField();
            answerField.setPromptText("Jawaban Anda");
            answerField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 50) {
                    answerField.setText(oldValue);
                    showAlert("Peringatan", "Jawaban tidak boleh lebih dari 50 karakter.");
                }
            });

            Button submitAnswerButton = new Button("Submit Jawaban");
            submitAnswerButton.setOnAction(e -> submitAnswer(answerField.getText()));


            Button finishQuizButton = new Button("Selesai Kuis");
            finishQuizButton.setOnAction(e -> finishQuiz());

            VBox quizPlayVBox = new VBox(10, playerNameLabel, currentQuestionLabel, answerField,
                    submitAnswerButton, finishQuizButton);
            quizPlayVBox.setAlignment(Pos.CENTER);

            scene4 = new Scene(quizPlayVBox, 400, 300);

            // Scene 5 (Quiz Summary Scene) menampilkan ringkasan kuis
            Label summaryLabel = new Label("Ringkasan Kuis");
            summaryLabel.setFont(new Font("Arial", 30));

            TableColumn<Quiz, String> summaryQuestionCol = createNonEditableColumn("Pertanyaan", "question");
            summaryQuestionCol.setMinWidth(200);
            TableColumn<Quiz, String> summaryAnswerCol = createNonEditableColumn("Jawaban Benar", "answer");
            summaryAnswerCol.setMinWidth(200);
            TableColumn<Quiz, String> summaryUserAnswerCol = createNonEditableColumn("Jawaban Anda", "userAnswer");
            summaryUserAnswerCol.setMinWidth(200);
            TableColumn<Quiz, String> summaryPointCol = createNonEditableColumn("Point", "point");
            summaryPointCol.setMinWidth(50);

            summaryTable.getColumns().addAll(summaryQuestionCol, summaryAnswerCol, summaryUserAnswerCol, summaryPointCol);

            summaryTable.setItems(summaryData);



            VBox summaryVBox = new VBox(10, summaryLabel, summaryTable);
            summaryVBox.setAlignment(Pos.CENTER);

            scene5 = new Scene(summaryVBox, 600, 400);

            primaryStage.setScene(scene2);
            primaryStage.show();
        }

        private TableColumn<Quiz, String> createEditableColumn(String title, String propertyName) {
            TableColumn<Quiz, String> column = new TableColumn<>(title);
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(e -> {
                e.getTableView().getItems().get(e.getTablePosition().getRow()).question.set(e.getNewValue());
                saveDataToFile();
            });
            return column;
        }

        private TableColumn<Quiz, String> createNonEditableColumn(String title, String propertyName) {
            TableColumn<Quiz, String> column = new TableColumn<>(title);
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
            return column;
        }

        private boolean isInputValid(TextField question, TextField answer, TextField point) {
            return !question.getText().isEmpty() &&
                    !answer.getText().isEmpty() &&
                    !point.getText().isEmpty();
        }

        private void showAlert(String title, String content) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }

        private void startQuiz() {
            playerScore = 0;
            currentQuestionIndex = 0;

            playerNameLabel.setText("Pemain: " + playerName.getText());
            showNextQuestion();

            primaryStage.setScene(scene4);
        }

        private void showNextQuestion() {
            if (currentQuestionIndex < data.size()) {
                Quiz currentQuestion = data.get(currentQuestionIndex);
                currentQuestionLabel.setText("Pertanyaan: " + currentQuestion.getQuestion());
                answerField.clear(); // Clear the answer field for the new question
            } else {
                finishQuiz();
            }
        }

        private void submitAnswer(String submittedAnswer) {
            if (currentQuestionIndex < data.size()) {
                Quiz currentQuestion = data.get(currentQuestionIndex);
                String correctAnswer = currentQuestion.getAnswer();

                if (submittedAnswer.equalsIgnoreCase(correctAnswer)) {
                    int points = Integer.parseInt(currentQuestion.getPoint());
                    playerScore += points;
                    showAlert("Benar!", "Jawaban Anda benar! Anda mendapatkan " + points + " poin.");
                } else {
                    showAlert("Salah", "Maaf, jawaban Anda salah.");
                }

                // Save user's answer
                userAnswers.add(new Quiz("", "", "", submittedAnswer));

                currentQuestionIndex++;
                showNextQuestion();
            } else {
                showAlert("Error", "Tidak ada pertanyaan lagi.");
            }
        }

        private void finishQuiz() {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Hasil Kuis");
            alert.setHeaderText(null);

            int totalPoints = 0;

            // Add user answers to summary
            for (Quiz question : data) {
                String userAnswer = "";
                if (userAnswers.size() > 0) {
                    userAnswer = userAnswers.get(0).getUserAnswer();
                    userAnswers.remove(0);
                }

                // Check if the user's answer is correct
                if (userAnswer.equalsIgnoreCase(question.getAnswer())) {
                    totalPoints += Integer.parseInt(question.getPoint());
                } else {
                    // Poin 0 jika jawaban salah
                    userAnswer = ""; // Reset user answer
                }

                summaryData.add(new Quiz(question.getQuestion(), question.getAnswer(), question.getPoint(), userAnswer));
            }

            alert.setContentText("Pemain: " + playerName.getText() + "\nSkor Anda: " + totalPoints);
            alert.showAndWait();

            // Save score to score.txt
            saveScore(playerName.getText(), totalPoints);

            // Show summary scene
            showSummary(totalPoints);
        }

        private void saveScore(String playerName, int score) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(scoreFile, true));

                String line = playerName + "," + score;

                writer.write(line);
                writer.newLine();

                writer.close();
            } catch (IOException e) {
                System.out.println("Error writing to file: " + scoreFile.getName());
            }
        }

        private void showSummary(int totalPoints) {
            Label totalPointsLabel = new Label("Total Poin: " + totalPoints);
            totalPointsLabel.setFont(new Font("Arial", 20));

            Button exitButton = new Button("Keluar");
            exitButton.setOnAction(e -> primaryStage.close());

            Button restartButton = new Button("Ulangi Kuis");
            restartButton.setOnAction(e -> restartQuiz());

            Button backButton5 = new Button("Kembali");
            backButton5.setOnAction(e -> {
                clearSummaryTable();
                // Mengosongkan tabel saat bermain kembali
                table.getItems().clear();
                primaryStage.setScene(scene2);
            });

            VBox summaryVBox = new VBox(10, new Label("Ringkasan Kuis"), summaryTable, totalPointsLabel, exitButton, restartButton, backButton5);
            summaryVBox.setAlignment(Pos.CENTER);

            scene5 = new Scene(summaryVBox, 600, 400);
            primaryStage.setScene(scene5);
        }

        private void restartQuiz() {
            userAnswers.clear();
            summaryData.clear();
            // Mengosongkan tabel saat bermain kembali

            primaryStage.setScene(scene2);
        }


        // Fungsi untuk membaca data dari file teks
        private void readDataFromFile() {
            try {
                Scanner scanner = new Scanner(dataFile);

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");

                    if (parts.length == 4) {
                        data.add(new Quiz(parts[0], parts[1], parts[2], parts[3]));
                    }
                }

                scanner.close();
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + dataFile.getName());
            }
        }


        // Fungsi untuk menyimpan data ke dalam file teks
        private void saveDataToFile() {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));

                for (Quiz question : data) {
                    String line = question.getQuestion() + "," +
                            question.getAnswer() + "," +
                            question.getPoint() + "," +
                            question.getUserAnswer();

                    writer.write(line);
                    writer.newLine();
                }

                writer.close();
            } catch (IOException e) {
                System.out.println("Error writing to file: " + dataFile.getName());
            }
        }

        private void deleteSelectedQuiz() {
            Quiz selectedQuiz = table.getSelectionModel().getSelectedItem();
            if (selectedQuiz != null) {
                data.remove(selectedQuiz);

                // Menyimpan data ke file teks setiap kali ada perubahan
                saveDataToFile();

                showAlert("Berhasil", "Kuis berhasil dihapus.");
            } else {
                showAlert("Peringatan", "Pilih kuis yang ingin dihapus.");
            }
        }

    private void importQuizData() {
        try {
            Path path = Paths.get("quiz_data.txt");
            Stream<String> lines = Files.lines(path);

            lines.forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    data.add(new Quiz(parts[0], parts[1], parts[2], ""));
                }
            });

            lines.close();
        } catch (IOException e) {
            System.out.println("Error reading from file: " + e.getMessage());
        }
    }

    private void clearSummaryTable() {
        summaryData.clear();
        summaryTable.getItems().clear();
    }

}