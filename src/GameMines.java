import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;

public class GameMines extends JFrame {

    final String TITLE_OF_PROGRAM = "Mines";
    final String SIGH_OF_FLAG = "f";
    final int BLOCK_SIZE = 30;//в пикселях
    final int FIELD_SIZE = 9;
    final int FIELD_DX = 6;
    final int FIELD_DY = 28 + 17;
    final int START_LOCATION = 200;
    final int MOUSE_BUTTON_LEFT = 1;
    final int MOUSE_BUTTON_RIGHT = 3;
    final int NUMBER_OF_MINES = 10;
    final int[] COLOR_OF_NUMBERS = {0x0000FF, 0x008000, 0xFF0000, 0x800000, 0x0}; //Цвет
    Cell[][] field = new Cell[FIELD_SIZE][FIELD_SIZE];//
    Random random = new Random();
    int countOpenedCells;
    boolean youWon, bangMine;
    int bangX, bangY;

    public static void main(String[] args) {
        new GameMines();
    }

    GameMines() {
        setTitle(TITLE_OF_PROGRAM);
        setDefaultCloseOperation(EXIT_ON_CLOSE);//Вызываем методы от унаследованного класса JFrame
        setBounds(START_LOCATION, START_LOCATION, FIELD_SIZE * BLOCK_SIZE + FIELD_DX, FIELD_SIZE * BLOCK_SIZE + FIELD_DY);
        setResizable(false);
        TimerLabel timeLabel = new TimerLabel();//создаём метку с таймером
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);// делаем по горизонтали
        Canvas canvas = new Canvas();//Создали панель
        canvas.setBackground(Color.white);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                int x = e.getX() / BLOCK_SIZE;
                int y = e.getY() / BLOCK_SIZE;
                if (e.getButton() == MOUSE_BUTTON_LEFT && !bangMine && !youWon)//левая кнопка мыши и проверяем не победили ли или не проиграли ли
                    if (field[y][x].isNotOpen()) {
                        openCells(x, y);
                        youWon = countOpenedCells == FIELD_SIZE * FIELD_SIZE - NUMBER_OF_MINES;//проверяем победу
                        if (bangMine) { // если взвёлся флаг или взорвалась мина
                            bangX = x;
                            bangY = y;
                        }
                    }
                if (e.getButton() == MOUSE_BUTTON_RIGHT) field[y][x].inverseFlag();//правая кнопка мыши
                if (bangMine || youWon) timeLabel.stopTimer();//Проигрыш
                canvas.repaint();
            }
        });
        add(BorderLayout.CENTER, canvas);//ставится в центр экрана
        add(BorderLayout.SOUTH, timeLabel);
        setVisible(true);//делает окно видимым
        initField();
    }

    void openCells(int x, int y) {
        if (x < 0 || x > FIELD_SIZE - 1 || y < 0 || y > FIELD_SIZE - 1) return;
        if (!field[y][x].isNotOpen()) return;
        field[y][x].open();
        if (field[y][x].getCountBomb() > 0 || bangMine) return;
        for (int dx = -1; dx < 2; dx++)
            for (int dy = -1; dy < 2; dy++) openCells(x + dx, y + dy);
    }

    void initField() { //Инициализируем поля метод
        int x, y, countMines = 0;
        // создаём ячейку для поля
        for (x = 0; x < FIELD_SIZE; x++) //создаёт каждую клеточку и в неё попадает объект (двухмерный массив объектов)
            for (y = 0; y < FIELD_SIZE; y++)
                field[y][x] = new Cell();
        //минное поле
        while (countMines < NUMBER_OF_MINES) { // рандомно выставляем мины
            do {
                x = random.nextInt(FIELD_SIZE);
                y = random.nextInt(FIELD_SIZE);
            } while (field[y][x].isMined());
            field[y][x].mine();
            countMines++;
        }
        //to count dangerous neighbors
        for (x = 0; x < FIELD_SIZE; x++) //Считаем мины вокруг
            for (y = 0; y < FIELD_SIZE; y++)
                if (!field[y][x].isMined()) {
                    int count = 0;
                    for (int dx = -1; dx < 2; dx++)
                        for (int dy = -1; dy < 2; dy++) {
                            int nX = x + dx;
                            int nY = y + dy;
                            if (nX < 0 || nY < 0 || nX > FIELD_SIZE - 1 || nY > FIELD_SIZE - 1) {
                                nX = x;
                                nY = y;
                            }
                            count += (field[nY][nX].isMined()) ? 1 : 0;
                        }
                    field[y][x].setCountBomb(count);
                }
    }

    class Cell {
        private boolean isOpen, isMine, isFlag;// свойства ячейка открыта,есть мина,флаг
        private int countBombNear;//количество бомб вблизи

        void open() { //открыть
            isOpen = true;
            bangMine = isMine;
            if (!isMine) countOpenedCells++;//если ячейка открыта
        }

        void mine() {//заминировать
            isMine = true;
        }

        boolean isNotOpen() {
            return !isOpen;
        }

        void inverseFlag() {
            isFlag = !isFlag;
        }

        boolean isMined() {
            return isMine;
        }

        void setCountBomb(int count) {
            countBombNear = count;
        }

        int getCountBomb() {
            return countBombNear;
        }

        void paintBomb(Graphics g, int x, int y, Color color) {
            g.setColor(color);
            g.fillRect(x * BLOCK_SIZE + 7, y * BLOCK_SIZE + 10, 18, 10);
            g.fillRect(x * BLOCK_SIZE + 11, y * BLOCK_SIZE + 6, 10, 18);
            g.fillRect(x * BLOCK_SIZE + 9, y * BLOCK_SIZE + 8, 14, 14);
            g.setColor(color.white);
            g.fillRect(x * BLOCK_SIZE + 11, y * BLOCK_SIZE + 10, 4, 4);
        }

        void paintString(Graphics g, String str, int x, int y, Color color) {
            g.setColor(color);
            g.setFont(new Font("", Font.BOLD, BLOCK_SIZE));
            g.drawString(str, x * BLOCK_SIZE + 8, y * BLOCK_SIZE + 26);
        }

        void paint(Graphics g, int x, int y) {
            g.setColor(Color.lightGray); //Рисуем прямоугольник
            g.drawRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            if (!isOpen) {
                if ((bangMine || youWon) && isMine) paintBomb(g, x, y, Color.black);
                else {
                    g.setColor(Color.lightGray);
                    g.fill3DRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, true);
                    if (isFlag) paintString(g, SIGH_OF_FLAG, x, y, Color.red);
                }
            } else if (isMine) paintBomb(g, x, y, bangMine ? Color.red : Color.black);
            else if (countBombNear > 0)
                paintString(g, Integer.toString(countBombNear), x, y, new Color(COLOR_OF_NUMBERS[countBombNear - 1]));
        }

    }

    class TimerLabel extends JLabel {
        Timer timer = new Timer();

        TimerLabel() {
            timer.scheduleAtFixedRate(timerTask, 0, 1000);
        }

        TimerTask timerTask = new TimerTask() {
            volatile int time;
            Runnable refresher = new Runnable() {
                public void run() {
                    TimerLabel.this.setText(String.format("%02d:%02d", time / 60, time % 60));
                }
            };

            public void run() {
                time++;
                SwingUtilities.invokeLater(refresher);
            }
        };

        void stopTimer() {
            timer.cancel();
        }
    }

    class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);//вызов родительского метода отрисовки
            for (int x = 0; x < FIELD_SIZE; x++)
                for (int y = 0; y < FIELD_SIZE; y++) field[y][x].paint(g, x, y);
        }
    }
}
