(function () {
    'use strict';

    var API_POLLS = '/poll-service';
    var userId = crypto.randomUUID();
    var pollsContainer = document.getElementById('polls-container');

    fetchQuestions();

    function fetchQuestions() {
        fetch(API_POLLS + '/questions')
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (questions) {
                pollsContainer.innerHTML = '';
                questions.forEach(function (q) {
                    renderPollForm(q);
                });
            })
            .catch(function (err) {
                pollsContainer.innerHTML =
                    '<p class="loading">Error loading questions: ' + err.message + '</p>';
                showToast('Error loading questions', 'error');
            });
    }

    function renderPollForm(question) {
        var card = document.createElement('div');
        card.className = 'question-card';

        var title = document.createElement('h3');
        title.textContent = question.text;
        card.appendChild(title);

        var form = document.createElement('form');
        form.dataset.questionId = question.id;

        question.answers.forEach(function (answer) {
            var option = document.createElement('div');
            option.className = 'answer-option';

            var radio = document.createElement('input');
            radio.type = 'radio';
            radio.name = 'answer-' + question.id;
            radio.value = answer;
            radio.id = 'radio-' + question.id + '-' + answer;

            var label = document.createElement('label');
            label.htmlFor = radio.id;
            label.textContent = answer;

            option.appendChild(radio);
            option.appendChild(label);
            form.appendChild(option);
        });

        var btn = document.createElement('button');
        btn.type = 'submit';
        btn.className = 'submit-btn';
        btn.textContent = 'Submit Answer';
        form.appendChild(btn);

        form.addEventListener('submit', function (e) {
            e.preventDefault();
            var selected = form.querySelector('input[type="radio"]:checked');
            if (!selected) {
                showToast('Please select an answer', 'error');
                return;
            }
            submitAnswer(question.id, selected.value, btn, form);
        });

        card.appendChild(form);
        pollsContainer.appendChild(card);
    }

    function submitAnswer(questionId, answer, btn, form) {
        btn.disabled = true;
        btn.textContent = 'Submitting...';

        fetch(API_POLLS + '/submit', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                questionId: questionId,
                answer: answer,
                userId: userId
            })
        })
        .then(function (res) {
            if (!res.ok) throw new Error('HTTP ' + res.status);
            showToast('Answer submitted!', 'success');
            var radios = form.querySelectorAll('input[type="radio"]');
            radios.forEach(function (r) { r.checked = false; });
        })
        .catch(function (err) {
            showToast('Error submitting answer: ' + err.message, 'error');
        })
        .finally(function () {
            btn.disabled = false;
            btn.textContent = 'Submit Answer';
        });
    }

    function showToast(message, type) {
        var toast = document.createElement('div');
        toast.className = 'toast ' + type;
        toast.textContent = message;
        document.body.appendChild(toast);

        requestAnimationFrame(function () {
            toast.classList.add('show');
        });

        setTimeout(function () {
            toast.classList.remove('show');
            setTimeout(function () {
                document.body.removeChild(toast);
            }, 300);
        }, 3000);
    }

})();
