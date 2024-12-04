export default class Timer {
  constructor(initialTime = 0) {
    this.time = parseInt(initialTime, 10);
    this.timer = this.initTimer();
    this._timeElement;
    this._callback;
  }
  get hours() {
    return String(parseInt(this.time / 3600 % 24, 10)).padStart(2, '0');
  }
  get minutes() {
    return String(parseInt(this.time / 60 % 60, 10)).padStart(2, '0');
  }

  get seconds() {
    return String(this.time % 60).padStart(2, '0');
  }

  set timeElement(element) {
    this._timeElement = element;
    this.updateDOM();
  }

  set callback(callback) {
    this._callback = callback;
  }

  initTimer() {
    return setInterval(() => {
      this.time -= 1;
      this.updateDOM();

      if (this.time <= 0) this.stopTimer();
    }, 1000);
  }

  updateDOM() {
    if (this._timeElement)
      this._timeElement.innerHTML = `${this.hours}ч ${this.minutes}м ${this.seconds}с`;
  }

  stopTimer() {
    clearInterval(this.timer);
    this._callback();
  }
}
